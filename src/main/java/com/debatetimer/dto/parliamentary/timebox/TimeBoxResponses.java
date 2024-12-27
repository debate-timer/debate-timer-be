package com.debatetimer.dto.parliamentary.timebox;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TimeBoxResponses(
        @ArraySchema(schema = @Schema(description = "테이블들", implementation = TimeBoxResponse.class))
        List<TimeBoxResponses> table
) {

}
