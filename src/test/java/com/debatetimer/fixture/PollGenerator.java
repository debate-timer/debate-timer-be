package com.debatetimer.fixture;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.repository.poll.PollJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PollGenerator {

    private final PollJpaRepository pollJpaRepository;

    public PollGenerator(final PollJpaRepository pollJpaRepository) {
        this.pollJpaRepository = pollJpaRepository;
    }

    public PollEntity generate(CustomizeTableEntity customizeTableEntity, PollStatus status) {
        Poll poll = new Poll(
                null,
                customizeTableEntity.getId(),
                customizeTableEntity.getMember().getId(),
                status,
                "찬성",
                "반대",
                "주제"
        );
        return pollJpaRepository.save(new PollEntity(poll));
    }
}
