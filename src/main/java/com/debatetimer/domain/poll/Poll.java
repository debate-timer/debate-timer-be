package com.debatetimer.domain.poll;

import com.debatetimer.domain.customize.Agenda;
import com.debatetimer.domain.customize.TeamName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Poll {

    private final Long id;
    private final long tableId;
    private final long memberId;
    private final PollStatus status;
    private final TeamName prosTeamName;
    private final TeamName consTeamName;
    private final Agenda agenda;

    public Poll(Long id, long tableId, long memberId, PollStatus status,
                String prosTeamName, String consTeamName, String agenda) {
        this(id, tableId, memberId, status, new TeamName(prosTeamName), new TeamName(consTeamName), new Agenda(agenda));
    }
}
