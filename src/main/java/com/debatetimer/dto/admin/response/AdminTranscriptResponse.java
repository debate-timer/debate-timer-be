package com.debatetimer.dto.admin.response;

import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptOverview;
import com.debatetimer.domain.transcript.TranscriptStatus;
import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

public record AdminTranscriptResponse(
        long id,
        long memberId,
        @Nullable String memberEmail,
        long tableId,
        @Nullable String tableName,
        @Nullable String agenda,
        SttMode sttMode,
        TranscriptStatus status,
        boolean refining,
        @Nullable Long sourceTranscriptId,
        LocalDateTime createdAt
) {

    public AdminTranscriptResponse(TranscriptOverview overview) {
        this(overview.id(), overview.memberId(), overview.memberEmail(), overview.tableId(), overview.tableName(),
                overview.agenda(), overview.sttMode(), overview.status(), overview.refining(),
                overview.sourceTranscriptId(), overview.createdAt());
    }
}
