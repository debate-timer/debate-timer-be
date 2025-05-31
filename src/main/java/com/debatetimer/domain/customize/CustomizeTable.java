package com.debatetimer.domain.customize;

import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CustomizeTable {

    private final Member member;
    private final TableName name;
    private final String agenda;
    private final TeamName prosTeamName;
    private final TeamName consTeamName;
    private final boolean warningBell;
    private final boolean finishBell;

    public CustomizeTable(
            Member member,
            String name,
            String agenda,
            String prosTeamName,
            String consTeamName,
            boolean warningBell,
            boolean finishBell
    ) {
        this.member = member;
        this.name = new TableName(name);
        this.agenda = agenda;
        this.prosTeamName = new TeamName(prosTeamName);
        this.consTeamName = new TeamName(consTeamName);
        this.warningBell = warningBell;
        this.finishBell = finishBell;
    }

    public CustomizeTableEntity toEntity() {
        return new CustomizeTableEntity(
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

    public String getProsTeamName() {
        return prosTeamName.getValue();
    }

    public String getConsTeamName() {
        return consTeamName.getValue();
    }

    public String getName() {
        return name.getValue();
    }
}
