package com.debatetimer.domain.transcript;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 배치 음성 인식 토큰을 크롬 음성 인식의 확정 문장과 같은 단위의 발화로 묶는다.
 * 문장 끝 부호가 나오거나 말이 오래 끊기면 발화를 나눈다.
 */
public class UtteranceAssembler {

    public static final long PAUSE_THRESHOLD_MS = 1_500;
    private static final Pattern SENTENCE_END = Pattern.compile("[.?!。？！…]$");

    public List<Utterance> assemble(List<SpeechToken> tokens) {
        List<Utterance> utterances = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        long startMs = 0;
        long lastEndMs = 0;

        for (SpeechToken token : tokens) {
            if (!text.isEmpty() && token.startMs() - lastEndMs >= PAUSE_THRESHOLD_MS) {
                flush(utterances, startMs, text);
            }
            if (text.isEmpty()) {
                startMs = token.startMs();
            }
            text.append(token.text());
            lastEndMs = token.endMs();
            if (SENTENCE_END.matcher(token.text().strip()).find()) {
                flush(utterances, startMs, text);
            }
        }
        flush(utterances, startMs, text);
        return utterances;
    }

    private void flush(List<Utterance> utterances, long startMs, StringBuilder text) {
        String sentence = text.toString().strip();
        text.setLength(0);
        // 부호만 남은 토큰은 발화로 보지 않는다.
        if (sentence.codePoints().anyMatch(Character::isLetterOrDigit)) {
            utterances.add(new Utterance(startMs, sentence));
        }
    }
}
