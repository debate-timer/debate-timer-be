package com.debatetimer.domain.recordingv2;

import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

/**
 * 사회자가 연 다기기 녹음 한 번. sessionKey는 기기들이 쓰는 외부 식별자이고, 참가 코드는 해시로만 보관한다.
 */
public record RecordingSessionV2(
        long id,
        String sessionKey,
        long memberId,
        long tableId,
        @Nullable String language,
        String joinCodeHash,
        RecordingSessionStatusV2 status,
        @Nullable Long transcriptId,
        @Nullable String errorMessage,
        @Nullable LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
