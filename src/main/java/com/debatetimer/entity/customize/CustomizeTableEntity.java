package com.debatetimer.entity.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "customize_table")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomizeTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;
    private String agenda;
    private String prosTeamName;
    private String consTeamName;
    private boolean warningBell;
    private boolean finishBell;

    @NotNull
    private LocalDateTime usedAt;

    public CustomizeTableEntity(
            Member member,
            String name,
            String agenda,
            String prosTeamName,
            String consTeamName,
            boolean warningBell,
            boolean finishBell,
            LocalDateTime usedAt
    ) {
        this.member = member;
        this.name = name;
        this.agenda = agenda;
        this.prosTeamName = prosTeamName;
        this.consTeamName = consTeamName;
        this.warningBell = warningBell;
        this.finishBell = finishBell;
        this.usedAt = usedAt;
    }

    public void updateTable(CustomizeTable renewTable) {
        this.name = renewTable.getName();
        this.agenda = renewTable.getAgenda();
        this.prosTeamName = renewTable.getProsTeamName();
        this.consTeamName = renewTable.getConsTeamName();
        this.warningBell = renewTable.isWarningBell();
        this.finishBell = renewTable.isFinishBell();
        this.usedAt = LocalDateTime.now();
    }

    public void updateUsedAt() {
        this.usedAt = LocalDateTime.now();
    }

    public TableType getType() {
        return TableType.CUSTOMIZE;
    }
}
