package com.debatetimer.service.recordingv2;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.SpeakerSeparator;
import com.debatetimer.domain.transcript.SpeechSegment;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptTableSnapshot;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.event.transcript.TranscriptCreatedEvent;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전사가 끝난 턴들로 속기록을 만든다. 만든 속기록은 기존과 같이 생성 이벤트로 다듬기·분석이 이어진다.
 * 속기록의 STT 모드 컬럼은 기존 값만 받으므로 SONIOX로 저장하고, V2 출처는 녹음 세션의 transcriptId로 찾는다.
 */
@Service
@RequiredArgsConstructor
public class TranscriptAssemblerV2 {

    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final TranscriptDomainRepository transcriptDomainRepository;
    private final RecordingV2DomainRepository recordingV2DomainRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SpeakerSeparator speakerSeparator = new SpeakerSeparator();

    /**
     * @return 만든 속기록 ID. 전사된 발언이 없으면 비어 있다.
     */
    @Transactional
    public Optional<Long> assemble(RecordingSessionV2 session, List<RecordingTurnV2> turns) {
        CustomizeTable table = customizeTableDomainRepository.getById(session.tableId());
        List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getCustomizeTimeBoxes(session.tableId());
        List<TranscriptLine> lines = speakerSeparator.separate(timeBoxes, toSegments(turns));
        if (lines.isEmpty()) {
            return Optional.empty();
        }

        Transcript transcript = transcriptDomainRepository.create(table.getId(), session.memberId(),
                TranscriptTableSnapshot.from(table), SttMode.SONIOX, lines);
        recordingV2DomainRepository.completeSession(session.id(), transcript.id());
        eventPublisher.publishEvent(new TranscriptCreatedEvent(transcript.id()));
        return Optional.of(transcript.id());
    }

    /**
     * 턴 전사 결과의 offsetMs는 각 턴 녹음 시작 기준이므로, 가장 먼저 시작한 턴 기준의 토론 경과 시간으로 바꾼다.
     */
    List<SpeechSegment> toSegments(List<RecordingTurnV2> turns) {
        List<RecordingTurnV2> doneTurns = turns.stream()
                .filter(turn -> turn.status() == RecordingTurnStatusV2.DONE)
                .toList();
        long debateStartedAt = doneTurns.stream()
                .mapToLong(RecordingTurnV2::startedAtEpochMs)
                .min()
                .orElse(0);
        return doneTurns.stream()
                .map(turn -> toSegment(turn, turn.startedAtEpochMs() - debateStartedAt))
                .toList();
    }

    private SpeechSegment toSegment(RecordingTurnV2 turn, long turnOffsetMs) {
        List<Utterance> utterances = turn.utterances().stream()
                .map(utterance -> new Utterance(turnOffsetMs + utterance.offsetMs(), utterance.text()))
                .toList();
        return new SpeechSegment(turn.timeBoxIndex(), turn.team(), utterances);
    }
}
