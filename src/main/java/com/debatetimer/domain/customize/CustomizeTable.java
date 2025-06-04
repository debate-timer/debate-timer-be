package com.debatetimer.domain.customize;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CustomizeTable {

    private final Long id;
    private final Member member;
    private final TableName name;
    private final Agenda agenda;
    private final TeamName prosTeamName;
    private final TeamName consTeamName;
    private final boolean warningBell;
    private final boolean finishBell;
    private final LocalDateTime usedAt;

    public CustomizeTable(
            Long id,
            Member member,
            String name,
            String agenda,
            String prosTeamName,
            String consTeamName,
            boolean warningBell,
            boolean finishBell,
            LocalDateTime usedAt
    ) {
        this.id = id;
        this.member = member;
        this.name = new TableName(name);
        this.agenda = new Agenda(agenda);
        this.prosTeamName = new TeamName(prosTeamName);
        this.consTeamName = new TeamName(consTeamName);
        this.warningBell = warningBell;
        this.finishBell = finishBell;
        this.usedAt = usedAt;
    }

    public CustomizeTable(
            Member member,
            String name,
            String agenda,
            String prosTeamName,
            String consTeamName,
            boolean warningBell,
            boolean finishBell,
            LocalDateTime usedAt
    ) {
        this(null, member, name, agenda, prosTeamName, consTeamName, warningBell, finishBell, usedAt);
    }

    public TableType getType() {
        return TableType.CUSTOMIZE;
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

    public String getAgenda() {
        return agenda.getValue();
    }
}
