package com.debatetimer.fixture;

import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.repository.poll.VoteJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VoteGenerator {

    private final VoteJpaRepository voteJpaRepository;

    public VoteGenerator(VoteJpaRepository voteJpaRepository) {
        this.voteJpaRepository = voteJpaRepository;
    }

    public VoteEntity generate(PollEntity pollEntity, VoteTeam team, String name) {
        VoteEntity vote = new VoteEntity(null, pollEntity, team, name, UUID.randomUUID().toString());
        return voteJpaRepository.save(vote);
    }
}
