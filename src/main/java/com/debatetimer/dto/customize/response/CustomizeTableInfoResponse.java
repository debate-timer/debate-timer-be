package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.dto.member.TableType;

public record CustomizeTableInfoResponse(
        String name,
        TableType type,
        String agenda,
        String prosTeamName,
        String consTeamName,
        boolean warningBell,
        boolean finishBell
) {

    public CustomizeTableInfoResponse(CustomizeTable customizeTable) {
        this(
                customizeTable.getName(),
                TableType.CUSTOMIZE,
                customizeTable.getAgenda(),
                customizeTable.getProsTeamName(),
                customizeTable.getConsTeamName(),
                customizeTable.isWarningBell(),
                customizeTable.isFinishBell()
        );
    }
}
