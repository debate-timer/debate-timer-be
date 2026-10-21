package com.debatetimer.dto.admin;

import com.debatetimer.domain.customize.CustomizeTableOverview;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminTablePageResponse(
        List<AdminTableResponse> tables,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static AdminTablePageResponse from(Page<CustomizeTableOverview> overviews) {
        return new AdminTablePageResponse(
                overviews.map(AdminTableResponse::new).getContent(),
                overviews.getNumber(),
                overviews.getSize(),
                overviews.getTotalElements(),
                overviews.getTotalPages()
        );
    }
}
