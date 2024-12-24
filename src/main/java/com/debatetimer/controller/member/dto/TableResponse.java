package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.parliamentary_debate.ParliamentaryTable;

public record TableResponse(String name, TableType type, int duration) {

    public TableResponse(ParliamentaryTable parliamentaryTable) {
        this(parliamentaryTable.getName(), TableType.PARLIAMENTARY, parliamentaryTable.getDuration());
    }
}
