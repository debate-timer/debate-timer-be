package com.debatetimer.domain.sharing;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 기록 중인 라이브 공유 한 번의 진행 상태
 * - 사회자 연결이 끊긴 뒤 재연결 유예 시간이 지나거나, 공유가 너무 오래 이어지면 만료된다.
 * - 마지막 타임박스까지 진행한 공유는 사회자가 직접 종료하지 않아도 끝까지 진행한 것으로 본다.
 * - 연결이 끊긴 채 만료되면 끊긴 시각을 종료 시각으로 본다.
 */
public class ActiveSharing {

    public static final Duration RECONNECT_GRACE = Duration.ofMinutes(1);
    public static final Duration STALE_THRESHOLD = Duration.ofHours(12);

    @Getter
    private final long logId;

    @Getter
    @NotNull
    private final String memberEmail;

    @Getter
    @NotNull
    private final String tableName;

    private final int timeBoxCount;

    @NotNull
    private final LocalDateTime startedAt;

    @Nullable
    private final Integer lastSequence;

    @Nullable
    private final LocalDateTime disconnectedAt;

    public ActiveSharing(
            long logId,
            String memberEmail,
            String tableName,
            int timeBoxCount,
            LocalDateTime startedAt
    ) {
        this(logId, memberEmail, tableName, timeBoxCount, startedAt, null, null);
    }

    private ActiveSharing(
            long logId,
            String memberEmail,
            String tableName,
            int timeBoxCount,
            LocalDateTime startedAt,
            @Nullable Integer lastSequence,
            @Nullable LocalDateTime disconnectedAt
    ) {
        this.logId = logId;
        this.memberEmail = memberEmail;
        this.tableName = tableName;
        this.timeBoxCount = timeBoxCount;
        this.startedAt = startedAt;
        this.lastSequence = lastSequence;
        this.disconnectedAt = disconnectedAt;
    }

    /**
     * 끊긴 공유가 마지막 타임박스까지 진행됐는지 판단할 수 있도록, 청중 화면에 표시될 타임박스 순서를 기록한다.
     */
    public ActiveSharing record(TimerEvent timerEvent) {
        TimerEventData data = timerEvent.getTimerEventData();
        if (data == null) {
            return this;
        }
        int displayedSequence = timerEvent.getEventType().toDisplayedSequence(data.getSequence());
        return new ActiveSharing(logId, memberEmail, tableName, timeBoxCount, startedAt, displayedSequence,
                disconnectedAt);
    }

    public ActiveSharing disconnect(LocalDateTime at) {
        if (disconnectedAt != null) {
            return this;
        }
        return new ActiveSharing(logId, memberEmail, tableName, timeBoxCount, startedAt, lastSequence, at);
    }

    public ActiveSharing reconnect() {
        return new ActiveSharing(logId, memberEmail, tableName, timeBoxCount, startedAt, lastSequence, null);
    }

    public boolean isReconnectExpired(LocalDateTime now) {
        return disconnectedAt != null && !now.isBefore(disconnectedAt.plus(RECONNECT_GRACE));
    }

    public boolean isExpired(LocalDateTime now) {
        return isReconnectExpired(now) || !now.isBefore(startedAt.plus(STALE_THRESHOLD));
    }

    // 타임박스 순서는 0부터 시작한다
    public boolean isCompleted() {
        return lastSequence != null && lastSequence >= timeBoxCount - 1;
    }

    public LocalDateTime endedAt(LocalDateTime now) {
        if (disconnectedAt == null) {
            return now;
        }
        return disconnectedAt;
    }
}
