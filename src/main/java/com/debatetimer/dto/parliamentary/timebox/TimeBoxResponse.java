package com.debatetimer.dto.parliamentary.timebox;

import io.swagger.v3.oas.annotations.media.Schema;

public record TimeBoxResponse(
        @Schema(description = "입장", example = "PROS")
        String stance,

        @Schema(description = "발언 유형", example = "OPENING")
        String type,

        @Schema(description = "발언 시간(초)", example = "175")
        long time,

        @Schema(description = "발언자 번호", example = "1", nullable = true)
        Long speakerNumber
) {

}
