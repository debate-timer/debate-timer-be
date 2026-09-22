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

    private final Map<Long, Instant> finishedRoomExpirations = new ConcurrentHashMap<>();
    private final Map<Long, AcceptedVersion> lastAcceptedVersions = new ConcurrentHashMap<>();
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
        lastAcceptedVersions.remove(roomId);
    }

    public void removeExpired() {
        finishedRoomExpirations.values().removeIf(this::isExpired);
        lastAcceptedVersions.values()
                .removeIf(accepted -> isExpired(accepted.acceptedAt().plus(VERSION_TTL)));
    }

    private boolean isExpired(Instant expiration) {
        return !clock.instant().isBefore(expiration);
    }

    private record AcceptedVersion(long version, Instant acceptedAt) {
    }
}
