package com.debatetimer.dto.transcript.request;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.transcript.SpeechSegment;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Utterance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.lang.Nullable;

public record TranscriptCreateRequest(
        @NotNull @Valid List<SegmentRequest> segments,
        @Nullable SttMode sttMode
) {

    public TranscriptCreateRequest(List<SegmentRequest> segments) {
        this(segments, null);
    }

    // STT 모드 선택 이전 클라이언트는 크롬 음성 인식만 썼으므로 RPC로 본다.
    public SttMode resolveSttMode() {
        return sttMode == null ? SttMode.RPC : sttMode;
    }

    public List<SpeechSegment> toSegments() {
        return segments.stream()
                .map(SegmentRequest::toDomain)
                .toList();
    }

    public record SegmentRequest(
            @PositiveOrZero int timeBoxIndex,
            @Nullable Stance team,
            @NotNull @Valid List<UtteranceRequest> utterances
    ) {

        private SpeechSegment toDomain() {
            List<Utterance> domainUtterances = utterances.stream()
                    .map(utterance -> new Utterance(utterance.offsetMs(), utterance.text()))
                    .toList();
            return new SpeechSegment(timeBoxIndex, team, domainUtterances);
        }
    }

    public record UtteranceRequest(
            @PositiveOrZero long offsetMs,
            @NotNull String text
    ) {
    }
}
