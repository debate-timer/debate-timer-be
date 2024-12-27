package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

public record TimeBoxResponses(
        @ArraySchema(schema = @Schema(description = "테이블들", implementation = TimeBoxResponse.class))
        List<TimeBoxResponse> table
) {

        public static TimeBoxResponses toResponses(ParliamentaryTimeBoxes parliamentaryTimeBoxes) {
                return parliamentaryTimeBoxes.getTimeBoxes()
                        .stream()
                        .map(TimeBoxResponse::new)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), TimeBoxResponses::new));
        }
}
