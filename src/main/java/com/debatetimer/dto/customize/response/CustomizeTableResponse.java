package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeTable;
import java.util.List;

public record CustomizeTableResponse(long id, CustomizeTableInfoResponse info, List<CustomizeTimeBoxResponse> table) {

    public CustomizeTableResponse(
            CustomizeTable customizeTable,
            List<CustomizeTimeBoxResponse> table
    ) {
        this(customizeTable.getId(), new CustomizeTableInfoResponse(customizeTable), table);
    }
}
