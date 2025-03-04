package com.debatetimer.dto.timebased.response;

import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.dto.member.TableType;

public record TimeBasedTableInfoResponse(
        String name,
        TableType type,
        String agenda,
        boolean warningBell,
        boolean finishBell
) {

    public TimeBasedTableInfoResponse(TimeBasedTable timeBasedTable) {
        this(
                timeBasedTable.getName(),
                TableType.TIME_BASED,
                timeBasedTable.getAgenda(),
                timeBasedTable.isWarningBell(),
                timeBasedTable.isFinishBell()
        );
    }
}
