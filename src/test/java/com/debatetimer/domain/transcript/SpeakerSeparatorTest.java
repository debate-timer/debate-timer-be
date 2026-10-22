package com.debatetimer.domain.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.customize.NormalTimeBox;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.customize.TimeBasedTimeBox;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpeakerSeparatorTest {

    private final List<CustomizeTimeBox> timeBoxes = List.of(
            new NormalTimeBox(Stance.PROS, "입론", "콜리", 120, List.of()),
            new NormalTimeBox(Stance.CONS, "입론", "비토", 120, List.of()),
            new TimeBasedTimeBox(Stance.NEUTRAL, "자유토론", null, 300, 60)
    );

    @Test
    void 일반_타임박스의_발언은_시간표의_입장과_발언자로_구분한다() {
        List<SpeechSegment> segments = List.of(
                new SpeechSegment(0, null, List.of(new Utterance(1_000, "찬성 입론입니다"))),
                new SpeechSegment(1, null, List.of(new Utterance(130_000, "반대 입론입니다")))
        );

        List<TranscriptLine> lines = new SpeakerSeparator().separate(timeBoxes, segments);

        assertAll(
                () -> assertThat(lines).hasSize(2),
                () -> assertThat(lines.get(0).stance()).isEqualTo(Stance.PROS),
                () -> assertThat(lines.get(0).speaker()).isEqualTo("콜리"),
                () -> assertThat(lines.get(0).speechType()).isEqualTo("입론"),
                () -> assertThat(lines.get(1).stance()).isEqualTo(Stance.CONS),
                () -> assertThat(lines.get(1).speaker()).isEqualTo("비토")
        );
    }

    @Test
    void 자유토론_타임박스의_발언은_발언_중이던_팀으로_구분한다() {
        List<SpeechSegment> segments = List.of(
                new SpeechSegment(2, Stance.CONS, List.of(new Utterance(250_000, "반대 측 반박입니다"))),
                new SpeechSegment(2, Stance.PROS, List.of(new Utterance(260_000, "찬성 측 재반박입니다")))
        );

        List<TranscriptLine> lines = new SpeakerSeparator().separate(timeBoxes, segments);

        assertAll(
                () -> assertThat(lines.get(0).stance()).isEqualTo(Stance.CONS),
                () -> assertThat(lines.get(0).speaker()).isNull(),
                () -> assertThat(lines.get(1).stance()).isEqualTo(Stance.PROS)
        );
    }

    @Test
    void 발언은_시간순으로_정렬하고_순번을_매긴다() {
        List<SpeechSegment> segments = List.of(
                new SpeechSegment(1, null, List.of(new Utterance(130_000, "나중 발언"))),
                new SpeechSegment(0, null, List.of(
                        new Utterance(1_000, "첫 발언"),
                        new Utterance(5_000, "두 번째 발언")
                ))
        );

        List<TranscriptLine> lines = new SpeakerSeparator().separate(timeBoxes, segments);

        assertAll(
                () -> assertThat(lines).extracting(TranscriptLine::text)
                        .containsExactly("첫 발언", "두 번째 발언", "나중 발언"),
                () -> assertThat(lines).extracting(TranscriptLine::index)
                        .containsExactly(0, 1, 2)
        );
    }

    @Test
    void 시간표에_없는_순서이거나_빈_발언은_제외한다() {
        List<SpeechSegment> segments = List.of(
                new SpeechSegment(9, null, List.of(new Utterance(1_000, "없는 타임박스"))),
                new SpeechSegment(0, null, List.of(new Utterance(2_000, "   "))),
                new SpeechSegment(0, null, List.of(new Utterance(3_000, " 남는 발언 ")))
        );

        List<TranscriptLine> lines = new SpeakerSeparator().separate(timeBoxes, segments);

        assertThat(lines).extracting(TranscriptLine::text)
                .containsExactly("남는 발언");
    }

    @Test
    void 자유토론에서_팀_정보가_없으면_중립으로_둔다() {
        List<SpeechSegment> segments = List.of(
                new SpeechSegment(2, null, List.of(new Utterance(250_000, "팀 정보 없음")))
        );

        List<TranscriptLine> lines = new SpeakerSeparator().separate(timeBoxes, segments);

        assertThat(lines.get(0).stance()).isEqualTo(Stance.NEUTRAL);
    }
}
