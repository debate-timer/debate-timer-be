package com.debatetimer.fixture.entity;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.repository.poll.PollRepository;
import org.springframework.stereotype.Component;

@Component
public class PollEntityGenerator {

    private final PollRepository pollRepository;

    public PollEntityGenerator(final PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    public PollEntity generate(CustomizeTableEntity customizeTableEntity, PollStatus status) {
        return generate(customizeTableEntity, status, "주제");
    }

    public PollEntity generate(CustomizeTableEntity customizeTableEntity, PollStatus status, String agenda) {
        Poll poll = new Poll(
                null,
                customizeTableEntity.getId(),
                customizeTableEntity.getMember().getId(),
                status,
                "찬성",
                "반대",
                agenda
        );
        return pollRepository.save(new PollEntity(poll));
    }
}
