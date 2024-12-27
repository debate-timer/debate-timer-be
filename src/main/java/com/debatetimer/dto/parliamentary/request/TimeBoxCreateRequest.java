package com.debatetimer.dto.parliamentary.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TimeBoxCreateRequest(
        @Schema(description = "입장", example = "PROS")
        String stance,

        @Schema(description = "발언 유형", example = "OPENING")
        String type,

        @Schema(description = "발언 시간(초)", example = "175")
        int time,

        @Schema(description = "발언자 번호", example = "1", nullable = true)
        Integer speakerNumber //TODO Long으로 변환은 어떤지?
) {

}
