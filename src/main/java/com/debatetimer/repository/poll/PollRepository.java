package com.debatetimer.repository.poll;

import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<PollEntity, Long> {

    Optional<PollEntity> findByIdAndMemberId(long id, long memberId);

    default PollEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }

    default PollEntity getByIdAndMemberId(long id, long memberId) {
        return findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }
}
