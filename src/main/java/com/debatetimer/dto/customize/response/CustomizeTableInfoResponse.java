package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTableEntity;
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

    public CustomizeTableInfoResponse(CustomizeTableEntity customizeTableEntity) {
        this(
                customizeTableEntity.getName(),
                TableType.CUSTOMIZE,
                customizeTableEntity.getAgenda(),
                customizeTableEntity.getProsTeamName(),
                customizeTableEntity.getConsTeamName(),
                customizeTableEntity.isWarningBell(),
                customizeTableEntity.isFinishBell()
        );
    }
}
