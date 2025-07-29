package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.Vote;
import com.debatetimer.domain.poll.VoteTeam;

public record VoteCreateResponse(
        long id,
        String name,
        String participateCode,
        VoteTeam team
) {

    public VoteCreateResponse(Vote vote) {
        this(vote.getId(), vote.getName().getValue(), vote.getCode().getValue(), vote.getTeam());
    }
}
