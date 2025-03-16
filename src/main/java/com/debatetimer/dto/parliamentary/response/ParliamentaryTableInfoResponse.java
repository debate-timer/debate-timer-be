package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.member.TableType;

public record ParliamentaryTableInfoResponse(String name, TableType type, String agenda, boolean warningBell,
                                             boolean finishBell) {

    public ParliamentaryTableInfoResponse(ParliamentaryTable parliamentaryTable) {
        this(
                parliamentaryTable.getName(),
                TableType.PARLIAMENTARY,
                parliamentaryTable.getAgenda(),
                parliamentaryTable.isWarningBell(),
                parliamentaryTable.isFinishBell()
        );
    }
}
