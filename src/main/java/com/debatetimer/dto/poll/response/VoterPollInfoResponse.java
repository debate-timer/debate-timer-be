package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.PollStatus;

public record VoterPollInfoResponse(
        long id,
        PollStatus status,
        String prosTeamName,
        String consTeamName,
        String participantCode,
        long totalCount,
        long prosCount,
        long consCount
) {

}
