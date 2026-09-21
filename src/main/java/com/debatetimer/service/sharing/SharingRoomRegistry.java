package com.debatetimer.service.sharing;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SharingRoomRegistry {

    static final Duration FINISHED_TTL = Duration.ofDays(1);

    private final Map<Long, Instant> finishedRoomExpirations = new ConcurrentHashMap<>();
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

    public void removeExpired() {
        finishedRoomExpirations.values().removeIf(this::isExpired);
    }

    private boolean isExpired(Instant expiration) {
        return !clock.instant().isBefore(expiration);
    }
}
