package com.debatetimer.scheduler;

import com.debatetimer.service.sharing.ChairmanSessionRegistry;
import com.debatetimer.service.sharing.SharingRoomRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SharingRoomCleanupScheduler {

    private static final long INTERVAL_MILLIS = 60 * 60 * 1000L;

    private final SharingRoomRegistry sharingRoomRegistry;
    private final ChairmanSessionRegistry chairmanSessionRegistry;

    @Scheduled(fixedRate = INTERVAL_MILLIS)
    public void cleanupExpiredRooms() {
        sharingRoomRegistry.removeExpired();
        chairmanSessionRegistry.removeExpired();
    }
}
