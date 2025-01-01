package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ParliamentaryTableCreateRequest(
        @Schema(description = "테이블 기본 정보", implementation = TableInfoCreateRequest.class)
        TableInfoCreateRequest info,

        @ArraySchema(schema = @Schema(description = "테이블 타임박스들", implementation = TimeBoxCreateRequest.class))
        List<TimeBoxCreateRequest> table
) {

    public ParliamentaryTable toTable(Member member) {
        return info.toTable(member, sumOfTime());
    }

    private int sumOfTime() {
        return table.stream()
                .mapToInt(TimeBoxCreateRequest::time)
                .sum();
    }

    public ParliamentaryTimeBoxes toTimeBoxes(ParliamentaryTable parliamentaryTable) {
        return IntStream.range(0, table.size())
                .mapToObj(i -> table.get(i).toTimeBox(parliamentaryTable, i + 1))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ParliamentaryTimeBoxes::new));
    }
}
