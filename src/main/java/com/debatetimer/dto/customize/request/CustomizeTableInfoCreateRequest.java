package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.TableName;
import com.debatetimer.domain.TeamName;
import com.debatetimer.domain.member.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomizeTableInfoCreateRequest(
        @NotBlank
        String name,

        @NotNull
        String agenda,

        @NotBlank
        String prosTeamName,

        @NotBlank
        String consTeamName,

        boolean warningBell,
        boolean finishBell
) {

    public DebateTable toTable(Member member) {
        return new DebateTable(member, new TableName(name), agenda, new TeamName(prosTeamName),
                new TeamName(consTeamName), warningBell, finishBell);
    }
}
