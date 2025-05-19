package com.debatetimer.domain.customize;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomizeTable {

    private static final String TABLE_NAME_REGEX = "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\s]+$";
    private static final String TEAM_NAME_REGEX = "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\s]+$";
    public static final int TABLE_NAME_MAX_LENGTH = 20;
    public static final int TEAM_NAME_MAX_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
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

    public CustomizeTable(
            Member member,
            String name,
            String agenda,
            boolean warningBell,
            boolean finishBell,
            String prosTeamName,
            String consTeamName
    ) {
        validateTableName(name);
        validateTeamName(prosTeamName);
        validateTeamName(consTeamName);
        this.member = member;
        this.name = name;
        this.agenda = agenda;
        this.prosTeamName = prosTeamName;
        this.consTeamName = consTeamName;
        this.warningBell = warningBell;
        this.finishBell = finishBell;
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

    private void validateTableName(String name) {
        if (name.length() > TABLE_NAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_NAME_LENGTH);
        }
        if (!name.matches(TABLE_NAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_NAME_FORM);
        }
    }

    private void validateTeamName(String teamName) {
        if (teamName.length() > TEAM_NAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TEAM_NAME_LENGTH);
        }
        if (!teamName.matches(TEAM_NAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TEAM_NAME_FORM);
        }
    }

    public TableType getType() {
        return TableType.CUSTOMIZE;
    }
}
