package com.debatetimer.service.recordingv2;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.RecordingDeviceV2;
import com.debatetimer.domain.recordingv2.RecordingSecretV2;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import com.debatetimer.dto.recordingv2.request.RecordingJoinRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingSessionCreateRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingTurnUploadRequestV2;
import com.debatetimer.dto.recordingv2.response.RecordingDeviceResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionCreateResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionResponseV2;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 다기기 녹음 V2. 사회자가 녹음 세션을 열면 청중 기기도 참가 코드로 참가해 같은 턴을 녹음해 올린다.
 * 턴마다 모인 후보 중 하나만 골라 백그라운드에서 전사하고({@link TurnProcessorV2}),
 * 사회자가 끝내면 전사된 턴으로 속기록을 만든다({@link RecordingFinalizerV2}).
 */
@Service
public class RecordingServiceV2 {

    // 턴 키는 S3 경로에 들어간다. 프론트는 타임박스 순서와 타이머를 시작한 공유 이벤트 버전으로 만든다.
    private static final Pattern TURN_KEY_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    private final RecordingV2Properties properties;
    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final RecordingV2DomainRepository recordingV2DomainRepository;
    private final RecordingSessionRegistryV2 registry;
    private final AudioSpoolV2 audioSpool;
    private final Clock clock;

    @Autowired
    public RecordingServiceV2(
            RecordingV2Properties properties,
            CustomizeTableDomainRepository customizeTableDomainRepository,
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            AudioSpoolV2 audioSpool
    ) {
        this(properties, customizeTableDomainRepository, recordingV2DomainRepository, registry, audioSpool,
                Clock.systemDefaultZone());
    }

    RecordingServiceV2(
            RecordingV2Properties properties,
            CustomizeTableDomainRepository customizeTableDomainRepository,
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            AudioSpoolV2 audioSpool,
            Clock clock
    ) {
        this.properties = properties;
        this.customizeTableDomainRepository = customizeTableDomainRepository;
        this.recordingV2DomainRepository = recordingV2DomainRepository;
        this.registry = registry;
        this.audioSpool = audioSpool;
        this.clock = clock;
    }

    public RecordingSessionCreateResponseV2 create(Member member, RecordingSessionCreateRequestV2 request) {
        validateEnabled();
        customizeTableDomainRepository.getByIdAndMember(request.tableId(), member);
        String joinCode = RecordingSecretV2.generateJoinCode();
        RecordingSessionV2 session = recordingV2DomainRepository.createSession(UUID.randomUUID().toString(),
                member.getId(), request.tableId(), request.language(), RecordingSecretV2.hash(joinCode));
        RecordingDeviceResponseV2 host = addDevice(session, true);
        return new RecordingSessionCreateResponseV2(session.sessionKey(), joinCode, host.deviceId(),
                host.deviceToken());
    }

    public RecordingDeviceResponseV2 join(String sessionKey, RecordingJoinRequestV2 request) {
        validateEnabled();
        RecordingSessionV2 session = recordingV2DomainRepository.getSession(sessionKey);
        if (!RecordingSecretV2.matches(request.joinCode(), session.joinCodeHash())) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_RECORDING_JOIN_CODE);
        }
        validateRecording(session);
        return addDevice(session, false);
    }

    /**
     * 사회자 기기가 토큰을 잃었을 때(새로고침, 서버 재시작) 사회자 기기로 다시 참가한다.
     */
    public RecordingDeviceResponseV2 rejoinHost(Member member, String sessionKey) {
        validateEnabled();
        RecordingSessionV2 session = getOwnedSession(member, sessionKey);
        validateRecording(session);
        return addDevice(session, true);
    }

    public void upload(String sessionKey, @Nullable String deviceToken, String turnKey,
                       RecordingTurnUploadRequestV2 request, @Nullable MultipartFile audio) {
        validateEnabled();
        if (!TURN_KEY_PATTERN.matcher(turnKey).matches()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_RECORDING_TURN);
        }
        if (audio == null || audio.isEmpty()) {
            throw new DTClientErrorException(ClientErrorCode.EMPTY_AUDIO);
        }
        RecordingDeviceV2 device = findDevice(sessionKey, deviceToken);
        RecordingSessionV2 session = recordingV2DomainRepository.getSession(sessionKey);
        validateAcceptingUploads(session);
        if (recordingV2DomainRepository.existsTurn(session.id(), turnKey)) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_TURN_CLOSED);
        }

        Path file = audioSpool.save(audio);
        AudioCandidateV2 candidate = new AudioCandidateV2(device.deviceId(), device.host(), file,
                audio.getContentType(), audio.getOriginalFilename(), audio.getSize(), request.durationMs(),
                request.startedAtEpochMs(), request.timeBoxIndex(), request.team(), request.toMetrics(),
                clock.instant());
        try {
            registry.addCandidate(sessionKey, turnKey, candidate)
                    .ifPresent(replaced -> audioSpool.delete(replaced.file()));
        } catch (RuntimeException exception) {
            audioSpool.delete(file);
            throw exception;
        }
    }

    /**
     * 사회자가 토론을 끝낸다. 청중 기기의 마지막 턴 업로드가 늦게 도착할 수 있어 수집 창 시간만큼은 업로드를 더 받고,
     * 그 뒤 남은 턴이 모두 처리되면 속기록을 만든다. 이미 끝낸 세션이면 그대로 둔다.
     */
    public void finish(Member member, String sessionKey) {
        validateEnabled();
        RecordingSessionV2 session = getOwnedSession(member, sessionKey);
        if (session.status().isRecording()) {
            recordingV2DomainRepository.finishSession(session.id(), LocalDateTime.now(clock));
        }
    }

    public RecordingSessionResponseV2 get(Member member, String sessionKey) {
        validateEnabled();
        RecordingSessionV2 session = getOwnedSession(member, sessionKey);
        return new RecordingSessionResponseV2(session, recordingV2DomainRepository.findTurns(session.id()));
    }

    private RecordingDeviceResponseV2 addDevice(RecordingSessionV2 session, boolean host) {
        String deviceId = RecordingSecretV2.generateDeviceId();
        String deviceToken = RecordingSecretV2.generateDeviceToken();
        registry.addDevice(session.id(), session.sessionKey(), deviceId, RecordingSecretV2.hash(deviceToken), host,
                properties.maxDevices());
        return new RecordingDeviceResponseV2(deviceId, deviceToken);
    }

    private RecordingDeviceV2 findDevice(String sessionKey, @Nullable String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_RECORDING_DEVICE);
        }
        return registry.findDevice(sessionKey, RecordingSecretV2.hash(deviceToken))
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.INVALID_RECORDING_DEVICE));
    }

    // 다른 회원의 세션은 있는지도 알리지 않는다.
    private RecordingSessionV2 getOwnedSession(Member member, String sessionKey) {
        RecordingSessionV2 session = recordingV2DomainRepository.getSession(sessionKey);
        if (session.memberId() != member.getId()) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_NOT_FOUND);
        }
        return session;
    }

    private void validateRecording(RecordingSessionV2 session) {
        if (!session.status().isRecording()) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_CLOSED);
        }
    }

    private void validateAcceptingUploads(RecordingSessionV2 session) {
        if (session.status().isRecording()) {
            return;
        }
        boolean inGrace = session.status() == RecordingSessionStatusV2.FINISHING
                && session.finishedAt() != null
                && LocalDateTime.now(clock).isBefore(session.finishedAt().plus(properties.collectWindow()));
        if (!inGrace) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_CLOSED);
        }
    }

    private void validateEnabled() {
        if (!properties.enabled()) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_V2_DISABLED);
        }
    }
}
