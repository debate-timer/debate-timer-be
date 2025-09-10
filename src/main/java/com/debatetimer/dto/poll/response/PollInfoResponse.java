package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteInfo;
import java.util.List;

public record PollInfoResponse(
        long id,
        PollStatus status,
        String prosTeamName,
        String consTeamName,
        long totalCount,
        long prosCount,
        long consCount,
        List<String> voterNames
) {

    public PollInfoResponse(Poll poll, VoteInfo voteInfo) {
        this(
                poll.getId(),
                poll.getStatus(),
                poll.getProsTeamName().getValue(),
                poll.getConsTeamName().getValue(),
                voteInfo.getTotalCount(),
                voteInfo.getProsCount(),
                voteInfo.getConsCount(),
                voteInfo.getVoterNames()
        );
    }
}
