package com.debatetimer.dto.recordingv2.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

public record RecordingSessionCreateRequestV2(
        @Positive long tableId,
        // 프론트 음성 인식 언어(ko-KR, en-US 등). 비어 있으면 한국어로 전사한다.
        @Nullable @Size(max = 16) String language
) {
}
