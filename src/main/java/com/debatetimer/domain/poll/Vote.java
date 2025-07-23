package com.debatetimer.domain.poll;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Vote {

    private final Long id;
    private final long pollId;
    private final VoteTeam team;
    private final ParticipantName name;
    private final ParticipateCode code;

    public Vote(Long id, long pollId, VoteTeam team, String name, String code) {
        this(id, pollId, team, new ParticipantName(name), new ParticipateCode(code));
    }
}
