package com.debatetimer.dto.admin.response;

import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * rawLines 는 보정 전 STT 원문, lines 는 보정한 속기록이다.
 */
public record AdminTranscriptDetailResponse(
        long id,
        long memberId,
        long tableId,
        String tableName,
        String agenda,
        String prosTeamName,
        String consTeamName,
        TranscriptStatus status,
        SttMode sttMode,
        boolean refining,
        @Nullable Long sourceTranscriptId,
        @Nullable String errorMessage,
        List<TranscriptLine> rawLines,
        List<TranscriptLine> lines,
        @Nullable JsonNode report
) {

    public AdminTranscriptDetailResponse(Transcript transcript, @Nullable JsonNode report) {
        this(
                transcript.id(),
                transcript.memberId(),
                transcript.tableId(),
                transcript.table().tableName(),
                transcript.table().agenda(),
                transcript.table().prosTeamName(),
                transcript.table().consTeamName(),
                transcript.status(),
                transcript.sttMode(),
                transcript.refining(),
                transcript.sourceTranscriptId(),
                transcript.errorMessage(),
                transcript.originalLines(),
                transcript.lines(),
                report
        );
    }
}
