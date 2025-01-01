package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import io.swagger.v3.oas.annotations.media.Schema;

public record TableInfoResponse(
        @Schema(description = "테이블 이름", example = "테이블1")
        String name,

        @Schema(description = "토론 주제", example = "촉법소년 연령 인하")
        String agenda
) {

    public TableInfoResponse(ParliamentaryTable parliamentaryTable) {
        this(parliamentaryTable.getName(), parliamentaryTable.getAgenda());
    }
}
