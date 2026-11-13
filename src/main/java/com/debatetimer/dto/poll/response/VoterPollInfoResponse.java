package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.poll.ParticipateCode;
import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteInfo;

public record VoterPollInfoResponse(
        long id,
        PollStatus status,
        String prosTeamName,
        String consTeamName,
        String participateCode,
        long totalCount,
        long prosCount,
        long consCount,
        TableInfoResponse tableInfo
) {

    public VoterPollInfoResponse(Poll poll, CustomizeTable table, VoteInfo voteInfo, ParticipateCode code) {
        this(
                poll.getId(),
                poll.getStatus(),
                poll.getProsTeamName().getValue(),
                poll.getConsTeamName().getValue(),
                code.getValue(),
                voteInfo.getTotalCount(),
                voteInfo.getProsCount(),
                voteInfo.getConsCount(),
                new TableInfoResponse(table, poll)
        );
    }
}
