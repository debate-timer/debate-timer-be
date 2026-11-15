package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.event.transcript.TranscriptAnalysisListener;
import com.debatetimer.scheduler.recordingv2.RecordingV2Scheduler;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = {
        "recording-v2.enabled=true",
        "recording-v2.spool-dir=build/test-recording-v2",
        "recording-v2.max-devices=3"
})
class RecordingFinalizerV2Test extends BaseServiceTest {

    @Autowired
    private RecordingFinalizerV2 finalizer;

    @Autowired
    private RecordingV2DomainRepository recordingV2DomainRepository;

    @Autowired
    private TranscriptDomainRepository transcriptDomainRepository;

    @MockitoBean
    private RecordingV2Scheduler recordingV2Scheduler;

    // 속기록 저장 뒤 비동기로 도는 보정·분석이 다음 테스트의 같은 ID 속기록 행을 덮어쓰지 않도록 막는다
    @MockitoBean
    private TranscriptAnalysisListener transcriptAnalysisListener;

    private Member member;
    private CustomizeTableEntity table;

    @BeforeEach
    void setUp() {
        member = memberGenerator.generate("host@email.com");
        table = customizeTableEntityGenerator.generate(member);
        customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
        customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 2);
    }

    @Test
    void 전사된_턴들을_토론_경과_시간_순으로_속기록으로_만든다() {
        RecordingSessionV2 session = finishedSession(LocalDateTime.now().minusMinutes(1));
        completeTurn(session, "1-5", 1, 70_000L, new Utterance(500, "두 번째 턴입니다."));
        completeTurn(session, "0-1", 0, 10_000L, new Utterance(1_000, "첫 번째 턴입니다."));

        finalizer.finalizeReadySessions();

        RecordingSessionV2 found = recordingV2DomainRepository.getSession(session.id());
        Transcript transcript = transcriptDomainRepository.getById(found.transcriptId());
        assertAll(
                () -> assertThat(found.status()).isEqualTo(RecordingSessionStatusV2.COMPLETED),
                () -> assertThat(transcript.sttMode()).isEqualTo(SttMode.SONIOX),
                () -> assertThat(transcript.lines()).extracting(line -> line.text())
                        .containsExactly("첫 번째 턴입니다.", "두 번째 턴입니다."),
                () -> assertThat(transcript.lines()).extracting(line -> line.offsetMs())
                        .containsExactly(1_000L, 60_500L)
        );
    }

    @Test
    void 끝낸_뒤_수집_창_시간이_지나지_않았으면_기다린다() {
        RecordingSessionV2 session = finishedSession(LocalDateTime.now());
        completeTurn(session, "0-1", 0, 0L, new Utterance(0, "발언입니다."));

        finalizer.finalizeReadySessions();

        assertThat(recordingV2DomainRepository.getSession(session.id()).status())
                .isEqualTo(RecordingSessionStatusV2.FINISHING);
    }

    @Test
    void 처리_중인_턴이_있으면_기다린다() {
        RecordingSessionV2 session = finishedSession(LocalDateTime.now().minusMinutes(1));
        recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);

        finalizer.finalizeReadySessions();

        assertThat(recordingV2DomainRepository.getSession(session.id()).status())
                .isEqualTo(RecordingSessionStatusV2.FINISHING);
    }

    @Test
    void 최대_대기_시간이_지나면_처리가_끝난_턴만으로_만든다() {
        RecordingSessionV2 session = finishedSession(LocalDateTime.now().minusMinutes(11));
        completeTurn(session, "0-1", 0, 0L, new Utterance(0, "발언입니다."));
        recordingV2DomainRepository.createTurn(session.id(), "1-2", 1, null, 0L, 1);

        finalizer.finalizeReadySessions();

        RecordingSessionV2 found = recordingV2DomainRepository.getSession(session.id());
        assertAll(
                () -> assertThat(found.status()).isEqualTo(RecordingSessionStatusV2.COMPLETED),
                () -> assertThat(transcriptDomainRepository.getById(found.transcriptId()).lines()).hasSize(1)
        );
    }

    @Test
    void 전사된_발언이_없으면_세션을_실패로_기록한다() {
        RecordingSessionV2 session = finishedSession(LocalDateTime.now().minusMinutes(1));
        RecordingTurnV2 turn = recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);
        recordingV2DomainRepository.failTurn(turn.id(), "전사 실패");

        finalizer.finalizeReadySessions();

        RecordingSessionV2 found = recordingV2DomainRepository.getSession(session.id());
        assertAll(
                () -> assertThat(found.status()).isEqualTo(RecordingSessionStatusV2.FAILED),
                () -> assertThat(found.transcriptId()).isNull()
        );
    }

    private RecordingSessionV2 finishedSession(LocalDateTime finishedAt) {
        RecordingSessionV2 session = recordingV2DomainRepository.createSession("session-" + finishedAt.getNano(),
                member.getId(), table.getId(), "ko-KR", "hash");
        recordingV2DomainRepository.finishSession(session.id(), finishedAt);
        return session;
    }

    private void completeTurn(RecordingSessionV2 session, String turnKey, int timeBoxIndex, long startedAt,
                              Utterance utterance) {
        RecordingTurnV2 turn = recordingV2DomainRepository.createTurn(session.id(), turnKey, timeBoxIndex,
                Stance.PROS, startedAt, 1);
        recordingV2DomainRepository.completeTurn(turn.id(), "device", startedAt, Stance.PROS, null,
                List.of(utterance));
    }
}
