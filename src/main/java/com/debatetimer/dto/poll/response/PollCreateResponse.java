package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.Poll;

public record PollCreateResponse(long id) {

    public PollCreateResponse(Poll poll) {
        this(poll.getId());
    }
}
