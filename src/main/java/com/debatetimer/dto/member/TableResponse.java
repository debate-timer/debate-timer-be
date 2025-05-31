package com.debatetimer.dto.member;

import com.debatetimer.entity.customize.CustomizeTableEntity;

public record TableResponse(long id, String name, TableType type, String agenda) {

    public TableResponse(CustomizeTableEntity debateTable) {
        this(
                debateTable.getId(),
                debateTable.getName(),
                debateTable.getType(),
                debateTable.getAgenda()
        );
    }
}
