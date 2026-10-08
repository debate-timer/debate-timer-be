package com.debatetimer.event.sharing;

import java.time.LocalDateTime;

/**
 * 공유 기록이 종료되었음을 알린다. 사회자가 직접 종료했거나, 마지막 타임박스에서 끊긴 뒤 돌아오지 않은 경우이다.
 */
public record SharingFinishedEvent(
        long sharingLogId,
        long memberId,
        String memberEmail,
        long tableId,
        String tableName,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        long durationSeconds,
        int audienceCount
) {
}
