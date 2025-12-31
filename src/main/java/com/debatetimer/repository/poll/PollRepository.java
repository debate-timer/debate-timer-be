package com.debatetimer.repository.poll;

import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface PollRepository extends Repository<PollEntity, Long> {

    PollEntity save(PollEntity pollEntity);

    Optional<PollEntity> findById(long id);

    Optional<PollEntity> findByIdAndMemberId(long id, long memberId);

    default PollEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }

    default PollEntity getByIdAndMemberId(long id, long memberId) {
        return findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollEntity p SET p.status = :doneStatus WHERE p.status = :status AND p.createdAt <= :threshold")
    void updateStatusToDoneForOldPolls(@Param("doneStatus") PollStatus doneStatus, @Param("status") PollStatus status,
                                       @Param("threshold") LocalDateTime threshold);
}
