package com.debatetimer.dto.admin.request;

import jakarta.validation.constraints.NotBlank;

public record PromptUpdateRequest(
        @NotBlank
        String content
) {
}
