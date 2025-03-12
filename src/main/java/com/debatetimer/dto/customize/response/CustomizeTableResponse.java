package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import java.util.List;

public record CustomizeTableResponse(long id, CustomizeTableInfoResponse info, List<CustomizeTimeBoxResponse> table) {

    public CustomizeTableResponse(
            CustomizeTable customizeTable,
            TimeBoxes timeBasedTimeBoxes
    ) {
        this(
                customizeTable.getId(),
                new CustomizeTableInfoResponse(customizeTable),
                toTimeBoxResponses(timeBasedTimeBoxes)
        );
    }

    private static List<CustomizeTimeBoxResponse> toTimeBoxResponses(TimeBoxes<CustomizeTimeBox> timeBoxes) {
        List<CustomizeTimeBox> customizeTimeBoxes = timeBoxes.getTimeBoxes();
        return customizeTimeBoxes
                .stream()
                .map(CustomizeTimeBoxResponse::new)
                .toList();
    }
}
