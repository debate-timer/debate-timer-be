package com.debatetimer.domain.poll;

import com.debatetimer.domain.customize.Agenda;
import com.debatetimer.domain.customize.TeamName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Poll {

    private final Long id;
    private final long tableId;
    private final long userId;
    private final PollStatus status;
    private final TeamName prosTeamName;
    private final TeamName consTeamName;
    private final Agenda agenda;

    public Poll(Long id, long tableId, long userId, PollStatus status, String prosTeamName, String consTeamName,
                String agenda) {
        this(id, tableId, userId, status, new TeamName(prosTeamName), new TeamName(consTeamName), new Agenda(agenda));
    }
}
