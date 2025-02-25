package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import java.util.List;

public record ParliamentaryTableResponse(long id, ParliamentaryTableInfoResponse info,
                                         List<ParliamentaryTimeBoxResponse> table) {

    public ParliamentaryTableResponse(
            ParliamentaryTable parliamentaryTable,
            ParliamentaryTimeBoxes parliamentaryTimeBoxes
    ) {
        this(
                parliamentaryTable.getId(),
                new ParliamentaryTableInfoResponse(parliamentaryTable),
                toTimeBoxResponses(parliamentaryTimeBoxes)
        );
    }

    private static List<ParliamentaryTimeBoxResponse> toTimeBoxResponses(
            ParliamentaryTimeBoxes parliamentaryTimeBoxes) {
        return parliamentaryTimeBoxes.getTimeBoxes()
                .stream()
                .map(ParliamentaryTimeBoxResponse::new)
                .toList();
    }
}
