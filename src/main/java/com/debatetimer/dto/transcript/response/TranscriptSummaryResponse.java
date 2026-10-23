package com.debatetimer.dto.transcript.response;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.domain.transcript.TranscriptSummary;
import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

/**
 * 토론 기록 목록의 한 항목. 시간표가 삭제됐으면 시간표 이름과 주제는 null이다.
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

    public TranscriptSummaryResponse(TranscriptSummary summary, @Nullable CustomizeTable table) {
        this(summary.id(), summary.tableId(), table == null ? null : table.getName(),
                table == null ? null : table.getAgenda(), summary.sttMode(), summary.status(), summary.refining(),
                summary.createdAt());
    }
}
