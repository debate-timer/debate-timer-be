package com.debatetimer.service.sharing;

import jakarta.annotation.Nullable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class SharingRoomRegistry {

    static final Duration FINISHED_TTL = Duration.ofDays(1);
    static final Duration VERSION_TTL = Duration.ofDays(1);
    static final Duration SYNC_REQUEST_TTL = Duration.ofDays(1);
    static final Duration SYNC_REQUEST_INTERVAL = Duration.ofMillis(500);
    private static final int ROOM_LOCK_STRIPES = 64;

    private final Map<Long, Instant> finishedRoomExpirations = new ConcurrentHashMap<>();
    private final Map<Long, AcceptedVersion> lastAcceptedVersions = new ConcurrentHashMap<>();
    private final Map<Long, Instant> lastSyncRequestedAt = new ConcurrentHashMap<>();
    private final Object[] roomLocks = createRoomLocks();
    private final Clock clock;

    public SharingRoomRegistry() {
        this(Clock.systemDefaultZone());
    }

    SharingRoomRegistry(Clock clock) {
        this.clock = clock;
    }

    public void markFinished(long roomId) {
        finishedRoomExpirations.put(roomId, clock.instant().plus(FINISHED_TTL));
    }

    public void reopen(long roomId) {
        finishedRoomExpirations.remove(roomId);
    }

    public boolean isFinished(long roomId) {
        Instant expiration = finishedRoomExpirations.get(roomId);
        if (expiration == null) {
            return false;
        }
        if (isExpired(expiration)) {
            finishedRoomExpirations.remove(roomId, expiration);
            return false;
        }
        return true;
    }

    /**
     * 룸에서 마지막으로 수락한 버전보다 큰 버전만 수락한다.
     * 같거나 작은 버전은 재전송으로 인한 중복이거나 순서가 뒤바뀐 오래된 이벤트로 본다.
     * 버전이 없는 이벤트는 하위 호환을 위해 항상 수락하며, 마지막 버전을 바꾸지 않는다.
     */
    public boolean acceptVersion(long roomId, @Nullable Long version) {
        if (version == null) {
            return true;
        }

        AtomicBoolean accepted = new AtomicBoolean(false);
        lastAcceptedVersions.compute(roomId, (id, lastAccepted) -> {
            if (lastAccepted != null && version <= lastAccepted.version()) {
                return lastAccepted;
            }
            accepted.set(true);
            return new AcceptedVersion(version, clock.instant());
        });
        return accepted.get();
    }

    /**
     * 사회자가 새로 공유를 시작하면 다른 기기(시계)에서 발행할 수 있으므로 버전 기준을 초기화한다.
     */
    public void resetVersion(long roomId) {
        runExclusively(roomId, () -> lastAcceptedVersions.remove(roomId));
    }

    /**
     * 룸별로 사회자에게 보내는 상태 공유 요청의 최소 간격을 강제한다.
     * 여러 청중이 동시에 입장하거나 재구독하면 요청이 그만큼 늘어나고, 사회자의 응답은 룸 전체로 중계되므로
     * 메시지가 크게 증폭된다. 짧은 간격의 뒤따르는 요청은 직전 요청의 응답이 대신하므로 버린다.
     *
     * @return 요청을 보내도 되면 true
     */
    public boolean tryAcquireSyncRequest(long roomId) {
        Instant now = clock.instant();
        Instant lastRequestedAt = lastSyncRequestedAt.compute(roomId, (id, lastRequested) -> {
            if (lastRequested != null && now.isBefore(lastRequested.plus(SYNC_REQUEST_INTERVAL))) {
                return lastRequested;
            }
            return now;
        });
        return now.equals(lastRequestedAt);
    }

    /**
     * 같은 룸의 작업(버전 수락·룸 상태 변경·중계, 버전 초기화)을 하나씩 실행한다.
     * 인바운드 메시지는 여러 스레드에서 동시에 처리될 수 있어, 낮은 버전의 상태 변경이나 중계가
     * 높은 버전보다 늦게 반영되는 것을 막는다. 룸 수만큼 락이 늘지 않도록 고정 개수의 락을 나눠 쓴다.
     */
    public void runExclusively(long roomId, Runnable action) {
        synchronized (roomLocks[Math.floorMod(Long.hashCode(roomId), ROOM_LOCK_STRIPES)]) {
            action.run();
        }
    }

    public void removeExpired() {
        finishedRoomExpirations.values().removeIf(this::isExpired);
        lastAcceptedVersions.values()
                .removeIf(accepted -> isExpired(accepted.acceptedAt().plus(VERSION_TTL)));
        lastSyncRequestedAt.values()
                .removeIf(requestedAt -> isExpired(requestedAt.plus(SYNC_REQUEST_TTL)));
    }

    private boolean isExpired(Instant expiration) {
        return !clock.instant().isBefore(expiration);
    }

    private static Object[] createRoomLocks() {
        Object[] locks = new Object[ROOM_LOCK_STRIPES];
        for (int i = 0; i < ROOM_LOCK_STRIPES; i++) {
            locks[i] = new Object();
        }
        return locks;
    }

    private record AcceptedVersion(long version, Instant acceptedAt) {
    }
}
