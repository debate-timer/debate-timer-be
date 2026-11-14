package com.debatetimer.service.recordingv2;

import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.domain.recordingv2.RecordingDeviceV2;
import com.debatetimer.domain.recordingv2.TurnCollectionV2;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 녹음 중인 세션의 참가 기기와 턴별 수집 상태를 메모리에 둔다. 공유 기능의 레지스트리처럼 앱 인스턴스 하나를 가정한다.
 * - 기기 토큰은 해시로만 들고 있는다.
 * - 닫힌 턴은 다시 열지 않도록 턴 키만 기억한다.
 * - 닫혀서 넘긴 턴이 DB에 기록될 때까지는 정리되지 않은 것으로 보아, 그 사이에 속기록을 만들지 않게 한다.
 * 재시작하면 상태를 잃는다. 이미 DB에 기록된 턴은 남고, 기기는 참가 코드로 다시 참가한다.
 */
@Component
public class RecordingSessionRegistryV2 {

    private final Map<String, SessionState> sessions = new HashMap<>();
    private final Clock clock;

    public RecordingSessionRegistryV2() {
        this(Clock.systemUTC());
    }

    RecordingSessionRegistryV2(Clock clock) {
        this.clock = clock;
    }

    public synchronized RecordingDeviceV2 addDevice(long sessionId, String sessionKey, String deviceId,
                                                    String tokenHash, boolean host, int maxDevices) {
        SessionState state = sessions.computeIfAbsent(sessionKey, key -> new SessionState(sessionId));
        if (state.devices.size() >= maxDevices) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_DEVICE_LIMIT_EXCEEDED);
        }
        RecordingDeviceV2 device = new RecordingDeviceV2(deviceId, host);
        state.devices.put(tokenHash, device);
        state.touch(clock.instant());
        return device;
    }

    public synchronized Optional<RecordingDeviceV2> findDevice(String sessionKey, String tokenHash) {
        return Optional.ofNullable(sessions.get(sessionKey))
                .map(state -> state.devices.get(tokenHash));
    }

    /**
     * 턴 후보를 받는다. 첫 후보면 그 턴의 수집 창을 연다.
     *
     * @return 같은 기기가 전에 올린 후보. 호출하는 쪽이 파일을 지운다.
     */
    public synchronized Optional<AudioCandidateV2> addCandidate(String sessionKey, String turnKey,
                                                                AudioCandidateV2 candidate) {
        SessionState state = sessions.get(sessionKey);
        if (state == null) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_RECORDING_DEVICE);
        }
        if (state.closedTurnKeys.contains(turnKey)) {
            throw new DTClientErrorException(ClientErrorCode.RECORDING_TURN_CLOSED);
        }
        state.touch(clock.instant());
        return state.collections.computeIfAbsent(turnKey, key -> new TurnCollectionV2(key, clock.instant()))
                .add(candidate);
    }

    /**
     * 참가 기기가 모두 올렸거나 수집 창 시간이 지난 턴을 닫아 넘긴다.
     */
    public synchronized List<ClosedTurnV2> drainClosable(Duration window) {
        Instant now = clock.instant();
        List<ClosedTurnV2> closed = new ArrayList<>();
        sessions.forEach((sessionKey, state) -> closed.addAll(state.drainClosable(sessionKey, now, window)));
        return closed;
    }

    // 넘긴 턴을 DB에 기록했다.
    public synchronized void markPersisted(String sessionKey) {
        SessionState state = sessions.get(sessionKey);
        if (state != null) {
            state.inFlightTurns--;
        }
    }

    // 모으는 중이거나 DB에 기록하기 전인 턴이 없다. 재시작 등으로 상태가 없으면 기다릴 턴도 없다.
    public synchronized boolean isSettled(String sessionKey) {
        SessionState state = sessions.get(sessionKey);
        return state == null || (state.collections.isEmpty() && state.inFlightTurns <= 0);
    }

    /**
     * 세션 상태를 지운다.
     *
     * @return 고르지 못하고 남은 후보. 호출하는 쪽이 파일을 지운다.
     */
    public synchronized List<AudioCandidateV2> remove(String sessionKey) {
        SessionState state = sessions.remove(sessionKey);
        return state == null ? List.of() : state.pendingCandidates();
    }

    /**
     * 오래 활동이 없는 세션 상태를 지운다.
     *
     * @return 고르지 못하고 남은 후보. 호출하는 쪽이 파일을 지운다.
     */
    public synchronized List<AudioCandidateV2> removeIdle(Duration ttl) {
        Instant threshold = clock.instant().minus(ttl);
        List<AudioCandidateV2> leftovers = new ArrayList<>();
        Iterator<SessionState> iterator = sessions.values().iterator();
        while (iterator.hasNext()) {
            SessionState state = iterator.next();
            if (state.lastActiveAt.isBefore(threshold)) {
                leftovers.addAll(state.pendingCandidates());
                iterator.remove();
            }
        }
        return leftovers;
    }

    private class SessionState {

        private final long sessionId;
        private final Map<String, RecordingDeviceV2> devices = new HashMap<>();
        private final Map<String, TurnCollectionV2> collections = new HashMap<>();
        private final Set<String> closedTurnKeys = new HashSet<>();
        private int inFlightTurns;
        private Instant lastActiveAt = clock.instant();

        private SessionState(long sessionId) {
            this.sessionId = sessionId;
        }

        private void touch(Instant now) {
            lastActiveAt = now;
        }

        private List<ClosedTurnV2> drainClosable(String sessionKey, Instant now, Duration window) {
            Set<String> deviceIds = devices.values().stream()
                    .map(RecordingDeviceV2::deviceId)
                    .collect(Collectors.toSet());
            List<ClosedTurnV2> closed = new ArrayList<>();
            Iterator<TurnCollectionV2> iterator = collections.values().iterator();
            while (iterator.hasNext()) {
                TurnCollectionV2 collection = iterator.next();
                if (collection.shouldClose(deviceIds, now, window)) {
                    closed.add(new ClosedTurnV2(sessionId, sessionKey, collection.getTurnKey(), collection.close()));
                    closedTurnKeys.add(collection.getTurnKey());
                    inFlightTurns++;
                    iterator.remove();
                }
            }
            return closed;
        }

        private List<AudioCandidateV2> pendingCandidates() {
            return collections.values().stream()
                    .flatMap(collection -> collection.close().stream())
                    .toList();
        }
    }
}
