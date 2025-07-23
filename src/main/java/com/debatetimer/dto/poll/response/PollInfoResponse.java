package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.PollStatus;

public record PollInfoResponse(
        long id,
        PollStatus status,
        String prosTeamName,
        String consTeamName,
        long totalCount,
        long prosCount,
        long consCount
) {

}
