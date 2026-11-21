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
     * 사회자가 토론을 끝낼 때 생성 중인 빈 속기록을 먼저 만들어 세션에 연결한다.
     * 턴 처리를 기다리는 동안에도 토론 기록 목록에 이번 토론이 생성 중으로 보이게 한다.
     */
    @Transactional
    public void preparePending(RecordingSessionV2 session) {
        CustomizeTable table = customizeTableDomainRepository.getById(session.tableId());
        Transcript transcript = transcriptDomainRepository.create(table.getId(), session.memberId(),
                TranscriptTableSnapshot.from(table), SttMode.SONIOX, List.of());
        recordingV2DomainRepository.linkTranscript(session.id(), transcript.id());
    }

    /**
     * 끝낼 때 만든 속기록이 있으면 발언을 채우고, 없으면(재시작 복구로 끝낸 세션 등) 새로 만든다.
     *
     * @return 속기록 ID. 전사된 발언이 없으면 비어 있다.
     */
    @Transactional
    public Optional<Long> assemble(RecordingSessionV2 session, List<RecordingTurnV2> turns) {
        CustomizeTable table = customizeTableDomainRepository.getById(session.tableId());
        List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getCustomizeTimeBoxes(session.tableId());
        List<TranscriptLine> lines = speakerSeparator.separate(timeBoxes, toSegments(turns));
        if (lines.isEmpty()) {
            return Optional.empty();
        }

        long transcriptId = fillOrCreate(session, table, lines);
        recordingV2DomainRepository.completeSession(session.id(), transcriptId);
        eventPublisher.publishEvent(new TranscriptCreatedEvent(transcriptId));
        return Optional.of(transcriptId);
    }

    /**
     * 세션을 실패로 기록한다. 끝낼 때 만든 속기록이 있으면 함께 실패로 남겨 목록에 생성 중으로 남지 않게 한다.
     */
    @Transactional
    public void fail(RecordingSessionV2 session, String errorMessage) {
        recordingV2DomainRepository.failSession(session.id(), errorMessage);
        if (session.transcriptId() != null) {
            transcriptDomainRepository.fail(session.transcriptId(), errorMessage);
        }
    }

    private long fillOrCreate(RecordingSessionV2 session, CustomizeTable table, List<TranscriptLine> lines) {
        if (session.transcriptId() != null) {
            transcriptDomainRepository.fillLines(session.transcriptId(), lines);
            return session.transcriptId();
        }
        return transcriptDomainRepository.create(table.getId(), session.memberId(),
                TranscriptTableSnapshot.from(table), SttMode.SONIOX, lines).id();
    }

    /**
     * 턴 전사 결과의 offsetMs는 각 턴 녹음 시작 기준이므로, 가장 먼저 시작한 턴 기준의 토론 경과 시간으로 바꾼다.
     * 기준은 실패한 턴까지 포함해 정한다. 첫 턴이 실패해도 이후 발언 시각이 앞당겨지지 않게 하기 위해서다.
     */
    List<SpeechSegment> toSegments(List<RecordingTurnV2> turns) {
        long debateStartedAt = turns.stream()
                .mapToLong(RecordingTurnV2::startedAtEpochMs)
                .min()
                .orElse(0);
        return turns.stream()
                .filter(turn -> turn.status() == RecordingTurnStatusV2.DONE)
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
