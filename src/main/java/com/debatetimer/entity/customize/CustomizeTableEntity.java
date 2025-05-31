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
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private String name;

    private String agenda;

    @NotBlank
    private String prosTeamName;

    @NotBlank
    private String consTeamName;

    private boolean warningBell;
    private boolean finishBell;

    @NotNull
    private LocalDateTime usedAt;

    public CustomizeTableEntity(CustomizeTable customizeTable) {
        this.member = customizeTable.getMember();
        this.name = customizeTable.getName();
        this.agenda = customizeTable.getAgenda();
        this.prosTeamName = customizeTable.getProsTeamName();
        this.consTeamName = customizeTable.getConsTeamName();
        this.warningBell = customizeTable.isWarningBell();
        this.finishBell = customizeTable.isFinishBell();
        this.usedAt = LocalDateTime.now();
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
