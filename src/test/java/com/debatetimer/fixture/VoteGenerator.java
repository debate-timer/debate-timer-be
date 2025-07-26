package com.debatetimer.fixture;

import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.repository.poll.VoteRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VoteGenerator {

    private final VoteRepository voteRepository;

    public VoteGenerator(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public VoteEntity generate(PollEntity pollEntity, VoteTeam team, String name) {
        return generate(pollEntity, team, name, UUID.randomUUID().toString());
    }

    public VoteEntity generate(PollEntity pollEntity, VoteTeam team, String name, String code) {
        VoteEntity vote = new VoteEntity(null, pollEntity, team, name, code);
        return voteRepository.save(vote);
    }
}
