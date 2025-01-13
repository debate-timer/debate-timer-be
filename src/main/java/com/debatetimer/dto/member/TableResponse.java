package com.debatetimer.dto.member;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;

public record TableResponse(String name, TableType type, int duration) {

    public TableResponse(ParliamentaryTable parliamentaryTable) {
        this(parliamentaryTable.getName(), TableType.PARLIAMENTARY, parliamentaryTable.getDuration());
    }
}
