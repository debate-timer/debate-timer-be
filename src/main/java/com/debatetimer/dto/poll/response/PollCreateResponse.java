package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;

public record PollCreateResponse(
        long id,
        PollStatus status,
        String prosTeamName,
        String consTeamName
) {

    public PollCreateResponse(Poll poll) {
        this(poll.getId(), poll.getStatus(), poll.getProsTeamName().getValue(), poll.getConsTeamName().getValue());
    }
}
