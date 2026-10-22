package com.debatetimer.domain.transcript;

import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.customize.Stance;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.lang.Nullable;

/**
 * 토론 시간표를 기준으로 발언자를 구분한다. 일반 타임박스는 시간표의 입장·발언자를, 자유토론은 발언 중이던 팀을 화자로 본다.
 */
public class SpeakerSeparator {

    public List<TranscriptLine> separate(List<CustomizeTimeBox> timeBoxes, List<SpeechSegment> segments) {
        List<TranscriptLine> unordered = segments.stream()
                .filter(segment -> isValidIndex(timeBoxes, segment.timeBoxIndex()))
                .flatMap(segment -> toLines(timeBoxes.get(segment.timeBoxIndex()), segment).stream())
                .sorted(Comparator.comparingLong(TranscriptLine::offsetMs))
                .toList();

        return IntStream.range(0, unordered.size())
                .mapToObj(index -> withIndex(unordered.get(index), index))
                .toList();
    }

    private boolean isValidIndex(List<CustomizeTimeBox> timeBoxes, int index) {
        return index >= 0 && index < timeBoxes.size();
    }

    private List<TranscriptLine> toLines(CustomizeTimeBox timeBox, SpeechSegment segment) {
        Stance stance = resolveStance(timeBox, segment.team());
        String speaker = timeBox.getBoxType().isTimeBased() ? null : timeBox.getSpeaker();
        return segment.utterances().stream()
                .filter(utterance -> utterance.text() != null && !utterance.text().isBlank())
                .map(utterance -> new TranscriptLine(
                        0,
                        segment.timeBoxIndex(),
                        timeBox.getSpeechType(),
                        stance,
                        speaker,
                        utterance.offsetMs(),
                        utterance.text().strip()
                ))
                .toList();
    }

    private Stance resolveStance(CustomizeTimeBox timeBox, @Nullable Stance team) {
        if (timeBox.getBoxType().isNotTimeBased()) {
            return timeBox.getStance();
        }
        if (team == null || team.isNeutralStance()) {
            return Stance.NEUTRAL;
        }
        return team;
    }

    private TranscriptLine withIndex(TranscriptLine line, int index) {
        return new TranscriptLine(index, line.timeBoxIndex(), line.speechType(), line.stance(), line.speaker(),
                line.offsetMs(), line.text());
    }
}
