package com.debatetimer.fixture;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.repository.poll.PollRepository;
import org.springframework.stereotype.Component;

@Component
public class PollGenerator {

    private final PollRepository pollRepository;

    public PollGenerator(final PollRepository pollRepository) {
        this.pollRepository = pollRepository;
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
        return pollRepository.save(new PollEntity(poll));
    }
}
