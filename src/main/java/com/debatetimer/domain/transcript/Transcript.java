package com.debatetimer.domain.transcript;

import java.util.List;
import org.springframework.lang.Nullable;

public record Transcript(
        Long id,
        long tableId,
        long memberId,
        TranscriptStatus status,
        SttMode sttMode,
        List<TranscriptLine> lines,
        @Nullable String reportJson,
        @Nullable String errorMessage
) {
}
