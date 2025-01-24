package com.debatetimer.dto.member;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;

public record TableResponse(long id, String name, TableType type, int duration) {

    public TableResponse(ParliamentaryTable parliamentaryTable) {
        this(
                parliamentaryTable.getId(),
                parliamentaryTable.getName(),
                TableType.PARLIAMENTARY,
                parliamentaryTable.getDuration()
        );
    }
}
