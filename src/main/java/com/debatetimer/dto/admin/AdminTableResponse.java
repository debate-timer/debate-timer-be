package com.debatetimer.dto.admin;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTableOverview;
import java.time.LocalDateTime;

public record AdminTableResponse(
        long id,
        String name,
        String agenda,
        String memberEmail,
        LocalDateTime createdAt,
        LocalDateTime usedAt
) {

    public AdminTableResponse(CustomizeTableOverview overview) {
        this(overview.getTable(), overview.getCreatedAt());
    }

    private AdminTableResponse(CustomizeTable table, LocalDateTime createdAt) {
        this(
                table.getId(),
                table.getName(),
                table.getAgenda(),
                table.getMember().getEmail(),
                createdAt,
                table.getUsedAt()
        );
    }
}
