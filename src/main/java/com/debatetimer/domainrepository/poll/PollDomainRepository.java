package com.debatetimer.domainrepository.poll;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.poll.PollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PollDomainRepository {

    private final PollRepository pollRepository;

    @Transactional
    public Poll create(Poll poll) {
        PollEntity pollEntity = new PollEntity(poll);
        return pollRepository.save(pollEntity)
                .toDomain();
    }

    @Transactional(readOnly = true)
    public Poll getByIdAndMemberId(long id, long memberId) {
        return findPoll(id, memberId)
                .toDomain();
    }

    @Transactional
    public Poll finishPoll(long pollId, long memberId) {
        PollEntity pollEntity = findPoll(pollId, memberId);
        pollEntity.updateToDone();
        return pollEntity.toDomain();
    }

    private PollEntity findPoll(long pollId, long memberId) {
        return pollRepository.findByIdAndMemberId(pollId, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.POLL_NOT_FOUND));
    }
}
