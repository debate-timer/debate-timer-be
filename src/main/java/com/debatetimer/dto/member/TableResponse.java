package com.debatetimer.dto.member;

import com.debatetimer.domain.DebateTable;

public record TableResponse(long id, String name, TableType type, int duration) {

    public TableResponse(DebateTable debateTable) {
        this(
                debateTable.getId(),
                debateTable.getName(),
                debateTable.getType(),
                debateTable.getDuration()
        );
    }
}
