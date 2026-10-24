package com.debatetimer.dto.transcript.response;

import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.domain.transcript.TranscriptSummary;
import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

/**
 * 토론 기록 목록의 한 항목. 시간표 이름과 주제는 속기록을 만들 때의 값이다. 스냅샷 도입 전에 지워진 시간표의 기록은 null이다.
 */
public record TranscriptSummaryResponse(
        long id,
        long tableId,
        @Nullable String tableName,
        @Nullable String agenda,
        SttMode sttMode,
        TranscriptStatus status,
        boolean refining,
        LocalDateTime createdAt
) {

    public TranscriptSummaryResponse(TranscriptSummary summary) {
        this(summary.id(), summary.tableId(), summary.tableName(), summary.agenda(), summary.sttMode(),
                summary.status(), summary.refining(), summary.createdAt());
    }
}
