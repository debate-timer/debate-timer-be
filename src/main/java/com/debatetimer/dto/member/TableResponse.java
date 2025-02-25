package com.debatetimer.dto.member;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.timebased.TimeBasedTable;

public record TableResponse(long id, String name, TableType type, int duration) {

    public TableResponse(ParliamentaryTable parliamentaryTable) {
        this(
                parliamentaryTable.getId(),
                parliamentaryTable.getName(),
                TableType.PARLIAMENTARY,
                parliamentaryTable.getDuration()
        );
    }

    public TableResponse(TimeBasedTable timeBasedTable) {
        this(
                timeBasedTable.getId(),
                timeBasedTable.getName(),
                TableType.TIME_BASED,
                timeBasedTable.getDuration()
        );
    }
}
