package com.debatetimer.domain.transcript;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class UtteranceAssemblerTest {

    private final UtteranceAssembler assembler = new UtteranceAssembler();

    @Test
    void 하위_단어_토큰을_이어_붙여_문장_끝_부호에서_발화를_나눈다() {
        List<SpeechToken> tokens = List.of(
                new SpeechToken("안", 100, 200),
                new SpeechToken("녕하십니까", 200, 600),
                new SpeechToken(".", 600, 620),
                new SpeechToken(" 찬성", 900, 1_200),
                new SpeechToken(" 입론", 1_200, 1_500),
                new SpeechToken("입니다?", 1_500, 1_800)
        );

        List<Utterance> utterances = assembler.assemble(tokens);

        assertThat(utterances).containsExactly(
                new Utterance(100, "안녕하십니까."),
                new Utterance(900, "찬성 입론입니다?")
        );
    }

    @Test
    void 말이_오래_끊기면_부호가_없어도_발화를_나눈다() {
        List<SpeechToken> tokens = List.of(
                new SpeechToken("첫", 0, 300),
                new SpeechToken(" 번째", 300, 600),
                new SpeechToken(" 두", 600 + UtteranceAssembler.PAUSE_THRESHOLD_MS, 2_400),
                new SpeechToken(" 번째", 2_400, 2_700)
        );

        List<Utterance> utterances = assembler.assemble(tokens);

        assertThat(utterances).containsExactly(
                new Utterance(0, "첫 번째"),
                new Utterance(600 + UtteranceAssembler.PAUSE_THRESHOLD_MS, "두 번째")
        );
    }

    @Test
    void 공백뿐인_발화는_버린다() {
        List<SpeechToken> tokens = List.of(
                new SpeechToken(" ", 0, 100),
                new SpeechToken(".", 100, 120)
        );

        assertThat(assembler.assemble(tokens)).isEmpty();
    }

    @Test
    void 토큰이_없으면_빈_목록을_돌려준다() {
        assertThat(assembler.assemble(List.of())).isEmpty();
    }
}
