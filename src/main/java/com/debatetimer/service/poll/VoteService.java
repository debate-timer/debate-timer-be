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
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
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
        validateProgressPoll(pollId);
        validateAlreadyVoted(pollId, voteRequest.participateCode());
        Vote vote = new Vote(pollId, voteRequest.team(), voteRequest.name(), voteRequest.participateCode());
        Vote savedVote = voteDomainRepository.save(vote);
        return new VoteCreateResponse(savedVote);
    }

    private void validateProgressPoll(long pollId) {
        Poll poll = pollDomainRepository.getById(pollId);
        if (!poll.isProgress()) {
            throw new DTClientErrorException(ClientErrorCode.ALREADY_DONE_POLL);
        }
    }

    private void validateAlreadyVoted(long pollId, String participateCode) {
        ParticipateCode code = new ParticipateCode(participateCode);
        if (voteDomainRepository.isExists(pollId, code)) {
            throw new DTClientErrorException(ClientErrorCode.ALREADY_VOTED_PARTICIPANT);
        }
    }

    @Transactional(readOnly = true)
    public VoterPollInfoResponse getVoterPollInfo(long pollId) {
        Poll poll = pollDomainRepository.getById(pollId);
        VoteInfo voteInfo = voteDomainRepository.findVoteInfoByPollId(pollId);
        ParticipateCode code = new ParticipateCode(UUID.randomUUID().toString());
        return new VoterPollInfoResponse(poll, voteInfo, code);
    }
}
