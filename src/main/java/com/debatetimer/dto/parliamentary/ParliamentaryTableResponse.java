package com.debatetimer.dto.parliamentary;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.dto.parliamentary.table.TableInfoResponse;
import com.debatetimer.dto.parliamentary.timebox.TimeBoxResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ParliamentaryTableResponse(
        @Schema(description = "테이블 아이디", example = "1")
        long id,

        @Schema(description = "테이블 기본 정보")
        TableInfoResponse info,

        @Schema(description = "테이블 타임박스들")
        TimeBoxResponses table
) {

    public ParliamentaryTableResponse(
            ParliamentaryTable parliamentaryTable,
            List<ParliamentaryTimeBox> parliamentaryTimeBoxes
    ) {
        this(
                parliamentaryTable.getId(),
                new TableInfoResponse(parliamentaryTable),
                TimeBoxResponses.toResponses(parliamentaryTimeBoxes)
        );
    }
}
