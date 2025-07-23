package com.debatetimer.domainrepository.poll;

import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.repository.poll.VoteJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteDomainRepository {

    private final VoteJpaRepository voteJpaRepository;

    public VoteInfo findVoteInfoByPollId(long pollId) {
        List<VoteEntity> pollVotes = voteJpaRepository.findAllByPollId(pollId);
        return resolveVoteInfo(pollId, pollVotes);
    }

    private VoteInfo resolveVoteInfo(long pollId, List<VoteEntity> voteEntities) {
        Map<VoteTeam, Long> teamCount = voteEntities.stream()
                .collect(Collectors.groupingBy(
                        VoteEntity::getTeam,
                        Collectors.counting()
                ));
        long prosCount = teamCount.get(VoteTeam.PROS);
        long consCount = teamCount.get(VoteTeam.CONS);
        return new VoteInfo(pollId, prosCount, consCount);
    }
}
