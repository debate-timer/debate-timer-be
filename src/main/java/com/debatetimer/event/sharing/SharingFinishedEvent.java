package com.debatetimer.event.sharing;

import java.time.LocalDateTime;

/**
 * 사회자가 공유 종료(FINISHED)를 명시적으로 발행해 공유 기록이 종료되었음을 알린다.
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
