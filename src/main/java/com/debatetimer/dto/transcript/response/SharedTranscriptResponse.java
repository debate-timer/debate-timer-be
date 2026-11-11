package com.debatetimer.dto.transcript.response;

import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.lang.Nullable;

// 공유 링크로 보는 속기록. 로그인 없이 열리므로 시간표 ID·오류 사유 같은 내부 정보는 뺀다.
public record SharedTranscriptResponse(
        String tableName,
        String agenda,
        String prosTeamName,
        String consTeamName,
        TranscriptStatus status,
        boolean refining,
        List<TranscriptLine> lines,
        @Nullable JsonNode report
) {

    public SharedTranscriptResponse(Transcript transcript, @Nullable JsonNode report) {
        this(transcript.table().tableName(), transcript.table().agenda(), transcript.table().prosTeamName(),
                transcript.table().consTeamName(), transcript.status(), transcript.refining(), transcript.lines(),
                report);
    }
}
