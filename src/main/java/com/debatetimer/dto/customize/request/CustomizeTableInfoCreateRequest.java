package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.CustomizeTable;
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

    public CustomizeTable toTable(Member member) {
        return new CustomizeTable(member, new TableName(name), agenda, new TeamName(prosTeamName),
                new TeamName(consTeamName), warningBell, finishBell);
    }
}
