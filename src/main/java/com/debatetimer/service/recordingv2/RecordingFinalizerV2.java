package com.debatetimer.service.recordingv2;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 사회자가 끝낸 녹음 세션을 속기록으로 마무리한다.
 * - 끝낸 뒤 수집 창 시간이 지나고, 모으는 중이거나 처리 중인 턴이 없으면 속기록을 만든다.
 * - 끝낸 뒤 최대 대기 시간이 지나면 처리가 끝난 턴만으로 만든다.
 * - 전사된 발언이 없거나 만들다 실패하면 세션을 실패로 기록한다.
 */
@Slf4j
@Service
public class RecordingFinalizerV2 {

    private final RecordingV2Properties properties;
    private final RecordingV2DomainRepository recordingV2DomainRepository;
    private final RecordingSessionRegistryV2 registry;
    private final TranscriptAssemblerV2 transcriptAssembler;
    private final AudioSpoolV2 audioSpool;
    private final Clock clock;

    @Autowired
    public RecordingFinalizerV2(
            RecordingV2Properties properties,
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            TranscriptAssemblerV2 transcriptAssembler,
            AudioSpoolV2 audioSpool
    ) {
        this(properties, recordingV2DomainRepository, registry, transcriptAssembler, audioSpool,
                Clock.systemDefaultZone());
    }

    RecordingFinalizerV2(
            RecordingV2Properties properties,
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            TranscriptAssemblerV2 transcriptAssembler,
            AudioSpoolV2 audioSpool,
            Clock clock
    ) {
        this.properties = properties;
        this.recordingV2DomainRepository = recordingV2DomainRepository;
        this.registry = registry;
        this.transcriptAssembler = transcriptAssembler;
        this.audioSpool = audioSpool;
        this.clock = clock;
    }

    public void finalizeReadySessions() {
        for (RecordingSessionV2 session : recordingV2DomainRepository.findSessions(RecordingSessionStatusV2.FINISHING)) {
            try {
                finalizeIfReady(session);
            } catch (RuntimeException exception) {
                log.warn("V2 녹음 세션을 마무리하지 못했습니다. sessionKey={}", session.sessionKey(), exception);
                recordingV2DomainRepository.failSession(session.id(), "속기록을 만들지 못했습니다.");
                cleanUp(session);
            }
        }
    }

    private void finalizeIfReady(RecordingSessionV2 session) {
        List<RecordingTurnV2> turns = recordingV2DomainRepository.findTurns(session.id());
        if (!isReady(session, turns)) {
            return;
        }
        Optional<Long> transcriptId = transcriptAssembler.assemble(session, turns);
        if (transcriptId.isEmpty()) {
            recordingV2DomainRepository.failSession(session.id(), "전사된 발언이 없습니다.");
        }
        cleanUp(session);
    }

    private boolean isReady(RecordingSessionV2 session, List<RecordingTurnV2> turns) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime finishedAt = session.finishedAt() == null ? now : session.finishedAt();
        if (!now.isBefore(finishedAt.plus(properties.finishTimeout()))) {
            return true;
        }
        boolean uploadClosed = !now.isBefore(finishedAt.plus(properties.collectWindow()));
        boolean processing = turns.stream().anyMatch(turn -> turn.status().isProcessing());
        return uploadClosed && registry.isSettled(session.sessionKey()) && !processing;
    }

    private void cleanUp(RecordingSessionV2 session) {
        registry.remove(session.sessionKey())
                .forEach(candidate -> audioSpool.delete(candidate.file()));
    }
}
