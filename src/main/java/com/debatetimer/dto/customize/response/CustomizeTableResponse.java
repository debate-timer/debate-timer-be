package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBoxEntities;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.List;

public record CustomizeTableResponse(long id, CustomizeTableInfoResponse info, List<CustomizeTimeBoxResponse> table) {

    public CustomizeTableResponse(
            CustomizeTable customizeTable,
            CustomizeTimeBoxEntities customizeTimeBoxes
    ) {
        this(customizeTable.getId(), new CustomizeTableInfoResponse(customizeTable),
                toTimeBoxResponses(customizeTimeBoxes));
    }

    public CustomizeTableResponse(
            CustomizeTable customizeTable,
            List<CustomizeTimeBoxResponse> timeBoxResponses
    ) {
        this(customizeTable.getId(), new CustomizeTableInfoResponse(customizeTable), timeBoxResponses);
    }

    private static List<CustomizeTimeBoxResponse> toTimeBoxResponses(CustomizeTimeBoxEntities timeBoxes) {
        List<CustomizeTimeBoxEntity> customizeTimeBoxes = timeBoxes.getTimeBoxes();
        return customizeTimeBoxes
                .stream()
                .map(CustomizeTimeBoxResponse::new)
                .toList();
    }
}
