package com.debatetimer.domainrepository.poll;

import com.debatetimer.domain.poll.Vote;
import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.repository.poll.PollRepository;
import com.debatetimer.repository.poll.VoteRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteDomainRepository {

    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;

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

    public Vote vote(Vote vote) {
        PollEntity pollEntity = pollRepository.getById(vote.getPollId());
        VoteEntity voteEntity = new VoteEntity(vote, pollEntity);
        return voteRepository.save(voteEntity)
                .toDomain();
    }
}
