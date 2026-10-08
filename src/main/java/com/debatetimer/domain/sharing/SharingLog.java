package com.debatetimer.domain.sharing;

import jakarta.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 라이브 공유 한 번(사회자의 공유 시작부터 종료까지)의 기록
 */
@Getter
@RequiredArgsConstructor
public class SharingLog {

    private final Long id;
    private final long tableId;
    private final long memberId;
    private final LocalDateTime startedAt;

    @Nullable
    private final LocalDateTime endedAt;

    private final int audienceCount;
    private final SharingLogStatus status;

    public SharingLog(long tableId, long memberId, LocalDateTime startedAt) {
        this(null, tableId, memberId, startedAt, null, 0, SharingLogStatus.SHARING);
    }

    public long getDurationSeconds() {
        if (endedAt == null) {
            return 0;
        }
        return Math.max(Duration.between(startedAt, endedAt).toSeconds(), 0);
    }
}
