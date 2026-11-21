package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.poll.Poll;

public record TableInfoResponse(
        long id,
        String name,
        String agenda
) {

    public TableInfoResponse(CustomizeTable table, Poll poll) {
        this(table.getId(), table.getName(), poll.getAgenda().getValue());
    }
}
