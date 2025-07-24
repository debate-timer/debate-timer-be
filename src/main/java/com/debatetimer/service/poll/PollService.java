package com.debatetimer.service.poll;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domainrepository.poll.CustomizeTableDomainRepository;
import com.debatetimer.domainrepository.poll.PollDomainRepository;
import com.debatetimer.domainrepository.poll.VoteDomainRepository;
import com.debatetimer.dto.poll.response.PollCreateResponse;
import com.debatetimer.dto.poll.response.PollInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PollService {

    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final PollDomainRepository pollDomainRepository;
    private final VoteDomainRepository voteDomainRepository;

    @Transactional
    public PollCreateResponse create(long tableId, Member member) {
        CustomizeTable table = customizeTableDomainRepository.getByIdAndMember(tableId, member);
        Poll poll = new Poll(null, table.getId(), member.getId(), PollStatus.PROGRESS,
                table.getProsTeamName(), table.getConsTeamName(), table.getAgenda());
        Poll savedPoll = pollDomainRepository.create(poll);
        return new PollCreateResponse(savedPoll);
    }

    @Transactional(readOnly = true)
    public PollInfoResponse readPollInfo(long pollId, Member member) {
        Poll poll = pollDomainRepository.getByIdAndMemberId(pollId, member.getId());
        VoteInfo voteInfo = voteDomainRepository.findVoteInfoByPollId(pollId);
        return new PollInfoResponse(poll, voteInfo);
    }

    @Transactional
    public PollInfoResponse finishPoll(long pollId, Member member) {
        Poll poll = pollDomainRepository.finishPoll(pollId, member.getId());
        VoteInfo voteInfo = voteDomainRepository.findVoteInfoByPollId(pollId);
        return new PollInfoResponse(poll, voteInfo);
    }
}
