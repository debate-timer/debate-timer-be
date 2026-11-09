package com.debatetimer.domain.transcript;

import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

/**
 * 토론 기록 목록에 보여 줄 요약. 속기록·보고서 본문은 담지 않는다.
 */
public record TranscriptSummary(
        long id,
        long tableId,
        @Nullable String tableName,
        @Nullable String agenda,
        SttMode sttMode,
        TranscriptStatus status,
        boolean refining,
        LocalDateTime createdAt
) {
}
