package com.debatetimer.dto.time_based.response;

import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBoxes;
import java.util.List;

public record TimeBasedTableResponse(long id, TimeBasedTableInfoResponse info, List<TimeBasedTimeBoxResponse> table) {

    public TimeBasedTableResponse(
            TimeBasedTable timeBasedTable,
            TimeBasedTimeBoxes timeBasedTimeBoxes
    ) {
        this(
                timeBasedTable.getId(),
                new TimeBasedTableInfoResponse(timeBasedTable),
                toTimeBoxResponses(timeBasedTimeBoxes)
        );
    }

    private static List<TimeBasedTimeBoxResponse> toTimeBoxResponses(TimeBasedTimeBoxes timeBasedTimeBoxes) {
        return timeBasedTimeBoxes.getTimeBoxes()
                .stream()
                .map(TimeBasedTimeBoxResponse::new)
                .toList();
    }
}
