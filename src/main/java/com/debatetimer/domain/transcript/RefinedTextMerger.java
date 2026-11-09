package com.debatetimer.domain.transcript;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * AI가 다듬은 문장을 속기록 줄에 반영한다. 화자·발언 순서·시각은 바꾸지 않고 문장만 바꾼다.
 * 군말만 있어 비운 줄은 빼고 번호를 다시 매긴다. 다듬은 결과가 비정상이면 원문을 지킨다.
 */
public class RefinedTextMerger {

    public List<TranscriptLine> merge(List<TranscriptLine> lines, Map<Integer, String> refinedTexts) {
        List<TranscriptLine> kept = lines.stream()
                .map(line -> withText(line, refinedTexts.getOrDefault(line.index(), line.text())))
                .filter(line -> !line.text().isBlank())
                .toList();
        if (kept.isEmpty()) {
            return lines;
        }
        return IntStream.range(0, kept.size())
                .mapToObj(index -> withIndex(kept.get(index), index))
                .toList();
    }

    private TranscriptLine withText(TranscriptLine line, String text) {
        String stripped = text == null ? "" : text.strip();
        return new TranscriptLine(line.index(), line.timeBoxIndex(), line.speechType(), line.stance(),
                line.speaker(), line.offsetMs(), stripped);
    }

    private TranscriptLine withIndex(TranscriptLine line, int index) {
        return new TranscriptLine(index, line.timeBoxIndex(), line.speechType(), line.stance(), line.speaker(),
                line.offsetMs(), line.text());
    }
}
