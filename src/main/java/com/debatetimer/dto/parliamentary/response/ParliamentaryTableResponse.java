package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import java.util.List;

public record ParliamentaryTableResponse(long id, TableInfoResponse info, List<TimeBoxResponse> table) {

    public ParliamentaryTableResponse(
            ParliamentaryTable parliamentaryTable,
            ParliamentaryTimeBoxes parliamentaryTimeBoxes
    ) {
        this(
                parliamentaryTable.getId(),
                new TableInfoResponse(parliamentaryTable),
                toTimeBoxResponses(parliamentaryTimeBoxes)
        );
    }

    private static List<TimeBoxResponse> toTimeBoxResponses(ParliamentaryTimeBoxes parliamentaryTimeBoxes) {
        return parliamentaryTimeBoxes.getTimeBoxes()
                .stream()
                .map(TimeBoxResponse::new)
                .toList();
    }
}
