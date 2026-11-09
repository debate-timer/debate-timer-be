package com.debatetimer.domain.transcript;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.customize.Stance;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RefinedTextMergerTest {

    private final RefinedTextMerger merger = new RefinedTextMerger();

    private final List<TranscriptLine> lines = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "김서연", 1_000, "안녕하십니까 찬송추 김서연입니다"),
            new TranscriptLine(1, 0, "입론", Stance.PROS, "김서연", 3_000, "어..."),
            new TranscriptLine(2, 1, "입론", Stance.CONS, "박지호", 65_000, "반대측 박지호입니다")
    );

    @Test
    void 다듬은_문장으로_바꾸고_화자와_시각은_그대로_둔다() {
        List<TranscriptLine> merged = merger.merge(lines, Map.of(
                0, "안녕하십니까, 찬성 측 김서연입니다.",
                1, "어...",
                2, "반대 측 박지호입니다."
        ));

        assertThat(merged).containsExactly(
                new TranscriptLine(0, 0, "입론", Stance.PROS, "김서연", 1_000, "안녕하십니까, 찬성 측 김서연입니다."),
                new TranscriptLine(1, 0, "입론", Stance.PROS, "김서연", 3_000, "어..."),
                new TranscriptLine(2, 1, "입론", Stance.CONS, "박지호", 65_000, "반대 측 박지호입니다.")
        );
    }

    @Test
    void 군말만_있어_비운_줄은_빼고_번호를_다시_매긴다() {
        List<TranscriptLine> merged = merger.merge(lines, Map.of(
                0, "안녕하십니까, 찬성 측 김서연입니다.",
                1, "  ",
                2, "반대 측 박지호입니다."
        ));

        assertThat(merged).extracting(TranscriptLine::index).containsExactly(0, 1);
        assertThat(merged).extracting(TranscriptLine::text)
                .containsExactly("안녕하십니까, 찬성 측 김서연입니다.", "반대 측 박지호입니다.");
        assertThat(merged.get(1).offsetMs()).isEqualTo(65_000);
    }

    @Test
    void 다듬은_결과에_없는_줄은_원문을_유지한다() {
        List<TranscriptLine> merged = merger.merge(lines, Map.of(0, "안녕하십니까, 찬성 측 김서연입니다."));

        assertThat(merged).extracting(TranscriptLine::text)
                .containsExactly("안녕하십니까, 찬성 측 김서연입니다.", "어...", "반대측 박지호입니다");
    }

    @Test
    void 모든_줄이_비면_원문을_그대로_돌려준다() {
        List<TranscriptLine> merged = merger.merge(lines, Map.of(0, "", 1, "", 2, ""));

        assertThat(merged).isEqualTo(lines);
    }
}
