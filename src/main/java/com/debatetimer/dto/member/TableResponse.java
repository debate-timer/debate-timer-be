package com.debatetimer.dto.member;

import com.debatetimer.domain.customize.CustomizeTable;

public record TableResponse(long id, String name, TableType type, String agenda) {

    public TableResponse(CustomizeTable customizeTable) {
        this(
                customizeTable.getId(),
                customizeTable.getName(),
                customizeTable.getType(),
                customizeTable.getAgenda()
        );
    }
}
