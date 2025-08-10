package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import java.util.List;

public record CustomizeTableResponse(long id, CustomizeTableInfoResponse info, List<CustomizeTimeBoxResponse> table) {

    public CustomizeTableResponse(CustomizeTable customizeTable,
                                  List<CustomizeTimeBox> customizeTimeBoxes) {
        this(customizeTable.getId(),
                new CustomizeTableInfoResponse(customizeTable),
                toTimeBoxResponses(customizeTimeBoxes));
    }

    private static List<CustomizeTimeBoxResponse> toTimeBoxResponses(List<CustomizeTimeBox> customizeTimeBoxes) {
        return customizeTimeBoxes
                .stream()
                .map(CustomizeTimeBoxResponse::new)
                .toList();
    }
}
