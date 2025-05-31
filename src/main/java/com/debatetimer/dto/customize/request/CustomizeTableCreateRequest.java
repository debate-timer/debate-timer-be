package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record CustomizeTableCreateRequest(
        @Valid CustomizeTableInfoCreateRequest info,
        @Valid List<CustomizeTimeBoxCreateRequest> table
) {

    public CustomizeTable toTable(Member member) {
        return info.toTable(member);
    }

    public CustomizeTimeBoxes toTimeBoxes(CustomizeTableEntity customizeTableEntity) {
        return IntStream.range(0, table.size())
                .mapToObj(i -> table.get(i).toTimeBox(customizeTableEntity, i + 1))
                .collect(Collectors.collectingAndThen(Collectors.toList(), CustomizeTimeBoxes::new));
    }
}
