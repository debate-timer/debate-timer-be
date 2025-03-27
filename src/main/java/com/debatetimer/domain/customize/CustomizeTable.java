package com.debatetimer.domain.customize;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomizeTable extends DebateTable {

    private static final String TEAM_NAME_REGEX = "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\s]+$";
    public static final int TEAM_NAME_MAX_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String prosTeamName;

    @NotBlank
    private String consTeamName;

    public CustomizeTable(
            Member member,
            String name,
            String agenda,
            boolean warningBell,
            boolean finishBell,
            String prosTeamName,
            String consTeamName
    ) {
        super(member, name, agenda, warningBell, finishBell);
        validateTeamName(prosTeamName);
        validateTeamName(consTeamName);
        this.prosTeamName = prosTeamName;
        this.consTeamName = consTeamName;
    }

    public void update(CustomizeTable renewTable) {
        this.prosTeamName = renewTable.getProsTeamName();
        this.consTeamName = renewTable.getConsTeamName();
        updateTable(renewTable);
    }

    private void validateTeamName(String teamName) {
        if (teamName.isBlank() || teamName.length() > TEAM_NAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TEAM_NAME_LENGTH);
        }
        if (!teamName.matches(TEAM_NAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TEAM_NAME_FORM);
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TableType getType() {
        return TableType.CUSTOMIZE;
    }
}
