package com.debatetimer.entity.sharing;

import com.debatetimer.domain.sharing.SharingLog;
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

    public SharingLogEntity(SharingLog sharingLog) {
        this.id = sharingLog.getId();
        this.tableId = sharingLog.getTableId();
        this.memberId = sharingLog.getMemberId();
        this.startedAt = sharingLog.getStartedAt();
        this.endedAt = sharingLog.getEndedAt();
        this.audienceCount = sharingLog.getAudienceCount();
        this.status = sharingLog.getStatus();
    }

    public SharingLog toDomain() {
        return new SharingLog(id, tableId, memberId, startedAt, endedAt, audienceCount, status);
    }
}
