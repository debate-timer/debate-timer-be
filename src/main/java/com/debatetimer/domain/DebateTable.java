package com.debatetimer.domain;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebateTable {

    private final Member member;
    private final TableName name;
    private final String agenda;
    private final TeamName prosTeamName;
    private final TeamName consTeamName;
    private final boolean warningBell;
    private final boolean finishBell;

    public CustomizeTable toEntity() {
        return new CustomizeTable(
                member,
                name.getValue(),
                agenda,
                prosTeamName.getValue(),
                consTeamName.getValue(),
                warningBell,
                finishBell,
                LocalDateTime.now()
        );
    }
}
