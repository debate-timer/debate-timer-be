package com.debatetimer.repository.sharing;

import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.entity.sharing.SharingLogEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SharingLogRepository extends Repository<SharingLogEntity, Long> {

    SharingLogEntity save(SharingLogEntity sharingLogEntity);

    Optional<SharingLogEntity> findById(long id);

    List<SharingLogEntity> findAll();

    List<SharingLogEntity> findAllByStatusAndStartedAtGreaterThanEqualAndStartedAtLessThan(
            SharingLogStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    default SharingLogEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.SHARING_LOG_NOT_FOUND));
    }

    // 여러 청중이 동시에 입장해도 누락되지 않도록 DB에서 원자적으로 증가시킨다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SharingLogEntity s SET s.audienceCount = s.audienceCount + 1 WHERE s.id = :id")
    void increaseAudienceCount(@Param("id") long id);

    // 진행 중인 기록만 바꿔, 다른 경로에서 먼저 끝난 기록을 덮어쓰지 않는다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SharingLogEntity s
            SET s.status = :status, s.endedAt = :endedAt
            WHERE s.id = :id AND s.status = :sharing
            """)
    int updateStatusIfSharing(
            @Param("id") long id,
            @Param("status") SharingLogStatus status,
            @Param("endedAt") LocalDateTime endedAt,
            @Param("sharing") SharingLogStatus sharing
    );

    // 서버 재시작 등으로 추적이 끊긴 공유는 종료 시각을 알 수 없으므로 마지막 수정 시각으로 종료 처리한다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SharingLogEntity s
            SET s.status = :abandoned, s.endedAt = s.modifiedAt
            WHERE s.status = :sharing AND s.startedAt < :threshold
            """)
    int abandonStaleSharings(
            @Param("abandoned") SharingLogStatus abandoned,
            @Param("sharing") SharingLogStatus sharing,
            @Param("threshold") LocalDateTime threshold
    );
}
