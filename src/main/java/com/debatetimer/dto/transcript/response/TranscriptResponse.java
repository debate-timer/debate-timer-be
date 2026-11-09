package com.debatetimer.dto.transcript.response;

import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.lang.Nullable;

public record TranscriptResponse(
        long id,
        long tableId,
        String tableName,
        String agenda,
        String prosTeamName,
        String consTeamName,
        TranscriptStatus status,
        SttMode sttMode,
        boolean refining,
        List<TranscriptLine> lines,
        @Nullable JsonNode report,
        @Nullable String errorMessage
) {

    // 시간표 정보는 속기록을 만들 때 저장한 스냅샷을 쓴다.
    public TranscriptResponse(Transcript transcript, @Nullable JsonNode report) {
        this(transcript.id(), transcript.tableId(), transcript.table().tableName(), transcript.table().agenda(),
                transcript.table().prosTeamName(), transcript.table().consTeamName(), transcript.status(), transcript.sttMode(), transcript.refining(),
                transcript.lines(), report, transcript.errorMessage());
    }
}
