package com.debatetimer.dto.member;

import com.debatetimer.domain.DebateTable;

public record TableResponse(long id, String name, TableType type, int duration) {

    public TableResponse(DebateTable debateTable, long id) {
        this(
                id,
                debateTable.getName(),
                debateTable.getType(),
                debateTable.getDuration()
        );
    }
}
