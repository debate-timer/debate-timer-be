package com.debatetimer.repository.poll;

import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface PollRepository extends Repository<PollEntity, Long> {

    PollEntity save(PollEntity pollEntity);

    Optional<PollEntity> findById(long id);

    Optional<PollEntity> findByIdAndMemberId(long id, long memberId);

    List<PollEntity> findAllByStatusAndCreatedAtBefore(PollStatus status, LocalDateTime createdAt);

    default PollEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }

    default PollEntity getByIdAndMemberId(long id, long memberId) {
        return findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }
}
