package com.debatetimer.service.sharing;

import jakarta.annotation.Nullable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 룸마다 발행 권한을 가진 사회자 세션을 하나만 둔다.
 * - 사회자 세션은 클라이언트가 공유를 시작할 때 발급하는 식별자로 구분하며, 같은 공유 중 재연결해도 유지된다.
 * - 다른 사회자 세션이 요청하면 최신 세션이 활성 세션을 넘겨받고, 이전 세션은 밀려난 세션으로 기록한다.
 * - 밀려난 세션은 재연결하더라도 활성 세션을 되찾을 수 없다.
 */
@Component
public class ChairmanSessionRegistry {

    static final Duration SESSION_TTL = Duration.ofDays(1);

    private final Map<Long, ChairmanHolder> holders = new HashMap<>();
    private final Map<Long, Map<String, Instant>> replacedSessions = new HashMap<>();
    private final Map<String, Long> roomIdsBySimpSession = new HashMap<>();
    private final Clock clock;

    public ChairmanSessionRegistry() {
        this(Clock.systemDefaultZone());
    }

    ChairmanSessionRegistry(Clock clock) {
        this.clock = clock;
    }

    public synchronized ChairmanClaim claim(long roomId, String chairmanSessionId, String simpSessionId) {
        if (isReplaced(roomId, chairmanSessionId)) {
            return ChairmanClaim.REJECTED;
        }

        ChairmanHolder previous = holders.get(roomId);
        registerHolder(roomId, chairmanSessionId, simpSessionId);
        if (previous == null) {
            return ChairmanClaim.NEW;
        }
        if (previous.chairmanSessionId().equals(chairmanSessionId)) {
            return ChairmanClaim.SAME;
        }

        replacedSessions.computeIfAbsent(roomId, id -> new HashMap<>())
                .put(previous.chairmanSessionId(), clock.instant());
        if (previous.isConnected()) {
            return ChairmanClaim.TAKEOVER;
        }
        return ChairmanClaim.NEW;
    }

    public synchronized boolean isActive(long roomId, String chairmanSessionId) {
        ChairmanHolder holder = holders.get(roomId);
        return holder != null && holder.isConnected() && holder.chairmanSessionId().equals(chairmanSessionId);
    }

    public synchronized boolean hasActiveChairman(long roomId) {
        return findActiveSessionId(roomId).isPresent();
    }

    public synchronized Optional<String> findActiveSessionId(long roomId) {
        return Optional.ofNullable(holders.get(roomId))
                .filter(ChairmanHolder::isConnected)
                .map(ChairmanHolder::chairmanSessionId);
    }

    /**
     * 연결 여부와 관계없이 마지막으로 활성 사회자였던 세션을 찾는다.
     * 밀려난 세션에게 발행 권한이 어느 세션으로 넘어갔는지 알려줄 때 쓴다.
     */
    public synchronized Optional<String> findLatestSessionId(long roomId) {
        return Optional.ofNullable(holders.get(roomId))
                .map(ChairmanHolder::chairmanSessionId);
    }

    /**
     * STOMP 연결이 끊기면 그 연결로 활성 사회자가 된 세션을 해제한다.
     * 재연결한 뒤에 옛 연결의 종료가 늦게 도착할 수 있어, 현재 연결과 일치할 때만 해제한다.
     */
    public synchronized void release(String simpSessionId) {
        Long roomId = roomIdsBySimpSession.remove(simpSessionId);
        if (roomId == null) {
            return;
        }
        ChairmanHolder holder = holders.get(roomId);
        if (holder != null && simpSessionId.equals(holder.simpSessionId())) {
            holders.put(roomId, holder.released(clock.instant()));
        }
    }

    /**
     * 룸의 사회자 기록(활성·밀려난 세션)을 모두 지운다.
     */
    public synchronized void remove(long roomId) {
        holders.remove(roomId);
        replacedSessions.remove(roomId);
        roomIdsBySimpSession.values().removeIf(id -> id == roomId);
    }

    public synchronized void removeExpired() {
        Instant now = clock.instant();
        holders.values().removeIf(holder -> !holder.isConnected() && isExpired(holder.releasedAt(), now));
        replacedSessions.values().forEach(sessions -> sessions.values()
                .removeIf(replacedAt -> isExpired(replacedAt, now)));
        replacedSessions.values().removeIf(Map::isEmpty);
    }

    private boolean isReplaced(long roomId, String chairmanSessionId) {
        return replacedSessions.getOrDefault(roomId, Map.of()).containsKey(chairmanSessionId);
    }

    private void registerHolder(long roomId, String chairmanSessionId, String simpSessionId) {
        ChairmanHolder previous = holders.put(roomId, new ChairmanHolder(chairmanSessionId, simpSessionId, null));
        if (previous != null && previous.simpSessionId() != null && !previous.simpSessionId().equals(simpSessionId)) {
            roomIdsBySimpSession.remove(previous.simpSessionId());
        }
        roomIdsBySimpSession.put(simpSessionId, roomId);
    }

    private boolean isExpired(Instant since, Instant now) {
        return !now.isBefore(since.plus(SESSION_TTL));
    }

    private record ChairmanHolder(
            String chairmanSessionId,
            String simpSessionId,
            @Nullable Instant releasedAt
    ) {

        private boolean isConnected() {
            return releasedAt == null;
        }

        private ChairmanHolder released(Instant releasedAt) {
            return new ChairmanHolder(chairmanSessionId, simpSessionId, releasedAt);
        }
    }
}
