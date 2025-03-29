package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import java.util.List;

public record ParliamentaryTableResponse(long id, ParliamentaryTableInfoResponse info,
                                         List<ParliamentaryTimeBoxResponse> table) {

    public ParliamentaryTableResponse(
            ParliamentaryTable parliamentaryTable,
            TimeBoxes<ParliamentaryTimeBox> parliamentaryTimeBoxes
    ) {
        this(
                parliamentaryTable.getId(),
                new ParliamentaryTableInfoResponse(parliamentaryTable),
                toTimeBoxResponses(parliamentaryTimeBoxes)
        );
    }

    private static List<ParliamentaryTimeBoxResponse> toTimeBoxResponses(TimeBoxes<ParliamentaryTimeBox> timeBoxes) {
        List<ParliamentaryTimeBox> parliamentaryTimeBoxes = timeBoxes.getTimeBoxes();
        return parliamentaryTimeBoxes
                .stream()
                .map(ParliamentaryTimeBoxResponse::new)
                .toList();
    }
}
