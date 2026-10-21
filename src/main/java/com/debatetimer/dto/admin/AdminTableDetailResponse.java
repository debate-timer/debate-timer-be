package com.debatetimer.dto.admin;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTableOverview;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.dto.customize.response.CustomizeTableInfoResponse;
import com.debatetimer.dto.customize.response.CustomizeTimeBoxResponse;
import java.time.LocalDateTime;
import java.util.List;

public record AdminTableDetailResponse(
        long id,
        String memberEmail,
        LocalDateTime createdAt,
        LocalDateTime usedAt,
        CustomizeTableInfoResponse info,
        List<CustomizeTimeBoxResponse> table
) {

    public AdminTableDetailResponse(CustomizeTableOverview overview, List<CustomizeTimeBox> timeBoxes) {
        this(overview.getTable(), overview.getCreatedAt(), timeBoxes);
    }

    private AdminTableDetailResponse(CustomizeTable table, LocalDateTime createdAt, List<CustomizeTimeBox> timeBoxes) {
        this(
                table.getId(),
                table.getMember().getEmail(),
                createdAt,
                table.getUsedAt(),
                new CustomizeTableInfoResponse(table),
                timeBoxes.stream()
                        .map(CustomizeTimeBoxResponse::new)
                        .toList()
        );
    }
}
