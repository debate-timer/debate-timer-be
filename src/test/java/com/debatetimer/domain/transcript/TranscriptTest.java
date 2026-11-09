package com.debatetimer.domain.transcript;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.customize.Stance;
import java.util.List;
import org.junit.jupiter.api.Test;

class TranscriptTest {

    private static final TranscriptTableSnapshot TABLE = new TranscriptTableSnapshot("테이블", "주제", "찬성", "반대");
    private static final List<TranscriptLine> RAW = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 0, "원문"));
    private static final List<TranscriptLine> REFINED = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 0, "보정본"));

    private Transcript transcript(List<TranscriptLine> lines, List<TranscriptLine> rawLines) {
        return new Transcript(1L, 1L, 1L, TABLE, TranscriptStatus.DONE, SttMode.RPC, false, lines, rawLines,
                null, null, null);
    }

    @Test
    void 보관한_원문이_있으면_원문을_쓴다() {
        assertThat(transcript(REFINED, RAW).originalLines()).isEqualTo(RAW);
    }

    @Test
    void 다듬기_도입_전_기록은_속기록을_원문으로_쓴다() {
        assertThat(transcript(RAW, List.of()).originalLines()).isEqualTo(RAW);
    }
}
