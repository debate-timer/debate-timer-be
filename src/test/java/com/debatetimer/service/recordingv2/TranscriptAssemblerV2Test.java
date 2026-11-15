package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.SpeechSegment;
import com.debatetimer.domain.transcript.Utterance;
import java.util.List;
import org.junit.jupiter.api.Test;

class TranscriptAssemblerV2Test {

    private final TranscriptAssemblerV2 assembler = new TranscriptAssemblerV2(null, null, null, null);

    @Test
    void 첫_턴이_실패해도_그_시작_시각을_토론_경과_시간의_기준으로_쓴다() {
        RecordingTurnV2 failed = turn(1L, 0, 10_000L, RecordingTurnStatusV2.FAILED, List.of());
        RecordingTurnV2 done = turn(2L, 1, 70_000L, RecordingTurnStatusV2.DONE, List.of(new Utterance(500, "반론")));

        List<SpeechSegment> segments = assembler.toSegments(List.of(failed, done));

        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).utterances()).extracting(Utterance::offsetMs)
                .containsExactly(60_500L);
    }

    private RecordingTurnV2 turn(long id, int timeBoxIndex, long startedAt, RecordingTurnStatusV2 status,
                                 List<Utterance> utterances) {
        return new RecordingTurnV2(id, 1L, timeBoxIndex + "-" + id, timeBoxIndex, null, startedAt, status, 1, null,
                null, utterances, null);
    }
}
