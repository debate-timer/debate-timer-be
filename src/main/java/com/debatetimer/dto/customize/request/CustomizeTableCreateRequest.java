package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
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

    public TimeBoxes toTimeBoxes(CustomizeTable customizeTable) {
        return IntStream.range(0, table.size())
                .mapToObj(i -> table.get(i).toTimeBox(customizeTable, i + 1))
                .collect(Collectors.collectingAndThen(Collectors.toList(), TimeBoxes::new));
    }
}
