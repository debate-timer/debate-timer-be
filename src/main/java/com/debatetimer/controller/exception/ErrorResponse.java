package com.debatetimer.controller.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "예외 객체")
public record ErrorResponse(

        @Schema(description = "사용자에게 보여줄 예외 메시지")
        @NotBlank
        String message
) {

}
