package com.debatetimer.domain.transcript;

import java.util.List;
import org.springframework.lang.Nullable;

public record Transcript(
        Long id,
        long tableId,
        long memberId,
        TranscriptTableSnapshot table,
        TranscriptStatus status,
        SttMode sttMode,
        // AI가 STT 원문을 다듬는 중이면 true. 다듬기가 끝나거나 실패하면 false가 된다.
        boolean refining,
        List<TranscriptLine> lines,
        // 다듬기 전 STT 원문. STT 모드별 인식 품질 비교용으로 보관한다.
        List<TranscriptLine> rawLines,
        @Nullable String reportJson,
        @Nullable String errorMessage,
        // 어드민 재실행으로 만든 기록이면 원본 속기록 ID
        @Nullable Long sourceTranscriptId
) {

    // 다듬기 도입 전 속기록은 원문을 따로 보관하지 않았지만, 그때는 lines 가 곧 STT 원문이다.
    public List<TranscriptLine> originalLines() {
        return rawLines.isEmpty() ? lines : rawLines;
    }
}
