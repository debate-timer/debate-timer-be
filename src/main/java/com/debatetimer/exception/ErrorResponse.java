package com.debatetimer.exception;

import jakarta.validation.constraints.NotBlank;

public record ErrorResponse(@NotBlank String message) {

}
