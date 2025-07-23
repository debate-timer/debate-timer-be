package com.debatetimer.service.poll;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domainrepository.poll.CustomizeTableDomainRepository;
import com.debatetimer.domainrepository.poll.PollDomainRepository;
import com.debatetimer.domainrepository.poll.VoteDomainRepository;
import com.debatetimer.dto.poll.response.PollCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PollService {

    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final PollDomainRepository pollDomainRepository;
    private final VoteDomainRepository voteDomainRepository;

    public PollCreateResponse create(long tableId, Member member) {
        CustomizeTable table = customizeTableDomainRepository.getByIdAndMember(tableId, member);
        Poll poll = new Poll(null, table.getId(), member.getId(), PollStatus.PROGRESS,
                table.getProsTeamName(), table.getConsTeamName(), table.getAgenda());
        Poll savedPoll = pollDomainRepository.create(poll);
        return new PollCreateResponse(savedPoll);
    }

//    public PollInfoResponse readPollInfo(long pollId) {
//        Poll poll = pollDomainRepository.findById(pollId);
//
//
//    }
}
