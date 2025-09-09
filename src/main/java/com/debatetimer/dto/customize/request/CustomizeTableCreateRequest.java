package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import jakarta.validation.Valid;
import java.util.List;

public record CustomizeTableCreateRequest(
        @Valid CustomizeTableInfoCreateRequest info,
        @Valid List<CustomizeTimeBoxCreateRequest> table
) {

    public List<CustomizeTimeBox> toTimeBoxes() {
        return table.stream()
                .map(CustomizeTimeBoxCreateRequest::toDomain)
                .toList();
    }

    public CustomizeTable toTable(Member member) {
        return info.toTable(member);
    }
}
