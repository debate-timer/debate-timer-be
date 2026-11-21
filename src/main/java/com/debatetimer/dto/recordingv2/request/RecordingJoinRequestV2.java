package com.debatetimer.dto.recordingv2.request;

import jakarta.validation.constraints.NotBlank;

public record RecordingJoinRequestV2(@NotBlank String joinCode) {
}
