package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ParliamentaryTableResponse(
        @Schema(description = "테이블 아이디", example = "1")
        long id,

        @Schema(description = "테이블 기본 정보", implementation = TableInfoResponse.class)
        TableInfoResponse info,

        @ArraySchema(schema = @Schema(description = "테이블들", implementation = TimeBoxResponse.class))
        List<TimeBoxResponse> table
) {

    public ParliamentaryTableResponse(
            ParliamentaryTable parliamentaryTable,
            ParliamentaryTimeBoxes parliamentaryTimeBoxes
    ) {
        this(
                parliamentaryTable.getId(),
                new TableInfoResponse(parliamentaryTable),
                toTimeBoxResponses(parliamentaryTimeBoxes)
        );
    }

    private static List<TimeBoxResponse> toTimeBoxResponses(ParliamentaryTimeBoxes parliamentaryTimeBoxes) {
        return parliamentaryTimeBoxes.getTimeBoxes()
                .stream()
                .map(TimeBoxResponse::new)
                .toList();
    }
}
