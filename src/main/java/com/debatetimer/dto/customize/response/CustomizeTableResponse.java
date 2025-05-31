package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTableEntity;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import java.util.List;

public record CustomizeTableResponse(long id, CustomizeTableInfoResponse info, List<CustomizeTimeBoxResponse> table) {

    public CustomizeTableResponse(
            CustomizeTableEntity customizeTableEntity,
            CustomizeTimeBoxes customizeTimeBoxes
    ) {
        this(
                customizeTableEntity.getId(),
                new CustomizeTableInfoResponse(customizeTableEntity),
                toTimeBoxResponses(customizeTimeBoxes)
        );
    }

    private static List<CustomizeTimeBoxResponse> toTimeBoxResponses(CustomizeTimeBoxes timeBoxes) {
        List<CustomizeTimeBox> customizeTimeBoxes = timeBoxes.getTimeBoxes();
        return customizeTimeBoxes
                .stream()
                .map(CustomizeTimeBoxResponse::new)
                .toList();
    }
}
