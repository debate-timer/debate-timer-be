package com.debatetimer.dto.transcript.response;

import com.debatetimer.domain.customize.CustomizeTable;
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
        List<TranscriptLine> lines,
        @Nullable JsonNode report,
        @Nullable String errorMessage
) {

    public TranscriptResponse(Transcript transcript, CustomizeTable table, @Nullable JsonNode report) {
        this(transcript.id(), transcript.tableId(), table.getName(), table.getAgenda(), table.getProsTeamName(),
                table.getConsTeamName(), transcript.status(), transcript.lines(), report, transcript.errorMessage());
    }
}
