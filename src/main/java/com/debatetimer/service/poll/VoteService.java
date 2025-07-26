package com.debatetimer.service.poll;

import com.debatetimer.domain.poll.ParticipateCode;
import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.Vote;
import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domainrepository.poll.PollDomainRepository;
import com.debatetimer.domainrepository.poll.VoteDomainRepository;
import com.debatetimer.dto.poll.request.VoteRequest;
import com.debatetimer.dto.poll.response.VoteCreateResponse;
import com.debatetimer.dto.poll.response.VoterPollInfoResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteDomainRepository voteDomainRepository;
    private final PollDomainRepository pollDomainRepository;

    @Transactional
    public VoteCreateResponse vote(long pollId, VoteRequest voteRequest) {
        Vote vote = new Vote(pollId, voteRequest.team(), voteRequest.name(), voteRequest.participateCode());
        Vote savedVote = voteDomainRepository.vote(vote);
        return new VoteCreateResponse(savedVote);
    }

    @Transactional(readOnly = true)
    public VoterPollInfoResponse getVoterPollInfo(long pollId) {
        Poll poll = pollDomainRepository.getById(pollId);
        VoteInfo voteInfo = voteDomainRepository.findVoteInfoByPollId(pollId);
        ParticipateCode code = new ParticipateCode(UUID.randomUUID().toString());
        return new VoterPollInfoResponse(poll, voteInfo, code);
    }
}
