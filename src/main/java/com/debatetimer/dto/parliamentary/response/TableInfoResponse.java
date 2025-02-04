package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;

public record TableInfoResponse(String name, String agenda, boolean warningBell, boolean finishBell) {

    public TableInfoResponse(ParliamentaryTable parliamentaryTable) {
        this(
                parliamentaryTable.getName(),
                parliamentaryTable.getAgenda(),
                parliamentaryTable.isWarningBell(),
                parliamentaryTable.isFinishBell()
        );
    }
}
