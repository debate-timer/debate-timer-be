package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import java.util.List;

public record CustomizeTableResponse(long id, CustomizeTableInfoResponse info, List<CustomizeTimeBoxResponse> table) {

    public CustomizeTableResponse(
            CustomizeTable customizeTable,
            CustomizeTimeBoxes customizeTimeBoxes
    ) {
        this(customizeTable.getId(), new CustomizeTableInfoResponse(customizeTable),
                toTimeBoxResponses(customizeTimeBoxes));
    }

    private static List<CustomizeTimeBoxResponse> toTimeBoxResponses(CustomizeTimeBoxes timeBoxes) {
        List<CustomizeTimeBox> customizeTimeBoxes = timeBoxes.getTimeBoxes();
        return customizeTimeBoxes
                .stream()
                .map(CustomizeTimeBoxResponse::new)
                .toList();
    }
}
