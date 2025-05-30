package com.debatetimer.dto.member;

import com.debatetimer.domain.customize.CustomizeTable;

public record TableResponse(long id, String name, TableType type, String agenda) {

    public TableResponse(CustomizeTable debateTable) {
        this(
                debateTable.getId(),
                debateTable.getName(),
                debateTable.getType(),
                debateTable.getAgenda()
        );
    }
}
