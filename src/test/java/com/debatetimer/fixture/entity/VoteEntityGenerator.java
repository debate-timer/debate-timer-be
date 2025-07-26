package com.debatetimer.fixture.entity;

import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.repository.poll.VoteRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VoteEntityGenerator {

    private final VoteRepository voteRepository;

    public VoteEntityGenerator(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public VoteEntity generate(PollEntity pollEntity, VoteTeam team, String name) {
        VoteEntity vote = new VoteEntity(null, pollEntity, team, name, UUID.randomUUID().toString());
        return voteRepository.save(vote);
    }
}
