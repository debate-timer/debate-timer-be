package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.parliamentary_debate.ParliamentaryTable;
import io.swagger.v3.oas.annotations.media.Schema;

public record TableResponse(
        @Schema(description = "테이블 이름", example = "테이블1")
        String name,

        @Schema(description = "토론 타입", example = "PARLIAMENTARY")
        TableType type,

        @Schema(description = "소요 시간 (초 단위)", example = "1800")
        int duration
) {

    public TableResponse(ParliamentaryTable parliamentaryTable) {
        this(parliamentaryTable.getName(), TableType.PARLIAMENTARY, parliamentaryTable.getDuration());
    }
}
