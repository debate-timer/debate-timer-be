package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;

public record TableInfoResponse(String name, String agenda) {

    public TableInfoResponse(ParliamentaryTable parliamentaryTable) {
        this(parliamentaryTable.getName(), parliamentaryTable.getAgenda());
    }
}
