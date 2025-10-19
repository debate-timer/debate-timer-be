package com.debatetimer.domain.poll;

import java.time.LocalDateTime;
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
    private final LocalDateTime createdAt;

    public Vote(long pollId, VoteTeam team, String name, String code) {
        this(null, pollId, team, name, code, LocalDateTime.now());
    }

    public Vote(Long id, long pollId, VoteTeam team, String name, String code, LocalDateTime createdAt) {
        this(id, pollId, team, new ParticipantName(name), new ParticipateCode(code), createdAt);
    }
}
