package com.debatetimer.entity.sharing;

import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 라이브 공유 한 번(사회자의 공유 시작부터 종료까지)을 기록한다.
 */
@Entity
@Getter
@Table(name = "sharing_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharingLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private long tableId;

    @Column(name = "member_id")
    private long memberId;

    @NotNull
    private LocalDateTime startedAt;

    @Nullable
    private LocalDateTime endedAt;

    private int audienceCount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SharingLogStatus status;

    public SharingLogEntity(long tableId, long memberId, LocalDateTime startedAt) {
        this.tableId = tableId;
        this.memberId = memberId;
        this.startedAt = startedAt;
        this.audienceCount = 0;
        this.status = SharingLogStatus.SHARING;
    }

    public void finish(LocalDateTime endedAt) {
        this.status = SharingLogStatus.FINISHED;
        this.endedAt = endedAt;
    }

    public void abandon(LocalDateTime endedAt) {
        this.status = SharingLogStatus.ABANDONED;
        this.endedAt = endedAt;
    }

    public long getDurationSeconds() {
        if (endedAt == null) {
            return 0;
        }
        return Math.max(Duration.between(startedAt, endedAt).toSeconds(), 0);
    }
}
