package com.debatetimer.domainrepository.poll;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.poll.PollJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PollDomainRepository {

    private final PollJpaRepository pollJpaRepository;

    public Poll create(Poll poll) {
        PollEntity pollEntity = new PollEntity(poll);
        return pollJpaRepository.save(pollEntity)
                .toDomain();
    }

    public Poll findById(long id) {
        return pollJpaRepository.findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND))
                .toDomain();
    }
}
