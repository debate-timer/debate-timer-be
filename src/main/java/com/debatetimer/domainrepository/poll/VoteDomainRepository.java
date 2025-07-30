package com.debatetimer.domainrepository.poll;

import com.debatetimer.domain.poll.ParticipateCode;
import com.debatetimer.domain.poll.Vote;
import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.decoder.RepositoryErrorDecoder;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.poll.PollRepository;
import com.debatetimer.repository.poll.VoteRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteDomainRepository {

    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final RepositoryErrorDecoder errorDecoder;

    public VoteInfo findVoteInfoByPollId(long pollId) {
        List<VoteEntity> pollVotes = voteRepository.findAllByPollId(pollId);
        return countVotes(pollId, pollVotes);
    }

    private VoteInfo countVotes(long pollId, List<VoteEntity> voteEntities) {
        Map<VoteTeam, Long> teamCount = voteEntities.stream()
                .collect(Collectors.groupingBy(VoteEntity::getTeam, Collectors.counting()));
        long prosCount = teamCount.getOrDefault(VoteTeam.PROS, 0L);
        long consCount = teamCount.getOrDefault(VoteTeam.CONS, 0L);
        return new VoteInfo(pollId, prosCount, consCount);
    }

    public boolean isExists(long pollId, ParticipateCode code) {
        return voteRepository.existsByPollIdAndParticipateCode(pollId, code.getValue());
    }

    public Vote save(Vote vote) {
        try {
            PollEntity pollEntity = pollRepository.getById(vote.getPollId());
            VoteEntity voteEntity = new VoteEntity(vote, pollEntity);
            return voteRepository.save(voteEntity)
                    .toDomain();
        } catch (DataIntegrityViolationException exception) {
            if (errorDecoder.isUniqueConstraintViolation(exception)) {
                throw new DTClientErrorException(ClientErrorCode.ALREADY_VOTED_PARTICIPANT);
            }
            throw exception;
        }
    }
}
