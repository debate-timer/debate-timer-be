package com.debatetimer.dto.timebased.response;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import java.util.List;

public record TimeBasedTableResponse(long id, TimeBasedTableInfoResponse info, List<TimeBasedTimeBoxResponse> table) {

    public TimeBasedTableResponse(
            TimeBasedTable timeBasedTable,
            TimeBoxes<TimeBasedTimeBox> timeBasedTimeBoxes
    ) {
        this(
                timeBasedTable.getId(),
                new TimeBasedTableInfoResponse(timeBasedTable),
                toTimeBoxResponses(timeBasedTimeBoxes)
        );
    }

    private static List<TimeBasedTimeBoxResponse> toTimeBoxResponses(TimeBoxes<TimeBasedTimeBox> timeBoxes) {
        List<TimeBasedTimeBox> timeBasedTimeBoxes = timeBoxes.getTimeBoxes();
        return timeBasedTimeBoxes
                .stream()
                .map(TimeBasedTimeBoxResponse::new)
                .toList();
    }
}
