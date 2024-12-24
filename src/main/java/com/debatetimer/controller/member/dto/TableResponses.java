package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.parliamentary_debate.ParliamentaryTable;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TableResponses(

        @ArraySchema(schema = @Schema(description = "테이블들", implementation = TableResponse.class))
        List<TableResponse> tables
) {

    public static TableResponses from(List<ParliamentaryTable> parliamentaryTables) {
        return new TableResponses(toTableResponses(parliamentaryTables));
    }

    private static List<TableResponse> toTableResponses(List<ParliamentaryTable> parliamentaryTables) {
        return parliamentaryTables.stream()
                .map(TableResponse::new)
                .toList();
    }
}
