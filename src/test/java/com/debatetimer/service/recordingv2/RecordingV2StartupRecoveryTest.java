package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import com.debatetimer.scheduler.recordingv2.RecordingV2Scheduler;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = {
        "recording-v2.enabled=true",
        "recording-v2.spool-dir=build/test-recording-v2",
        "recording-v2.max-devices=3"
})
class RecordingV2StartupRecoveryTest extends BaseServiceTest {

    @Autowired
    private RecordingV2StartupRecovery recovery;

    @Autowired
    private RecordingV2DomainRepository recordingV2DomainRepository;

    @MockitoBean
    private RecordingV2Scheduler recordingV2Scheduler;

    @Test
    void 처리_제한_시간보다_오래_처리_중인_턴을_실패로_바꾼다() {
        RecordingSessionV2 session = recordingV2DomainRepository.createSession("session", 1L, 2L, null, "hash");
        recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);

        recovery.recover(LocalDateTime.now().plusMinutes(6));

        RecordingTurnV2 turn = recordingV2DomainRepository.findTurns(session.id()).get(0);
        assertAll(
                () -> assertThat(turn.status()).isEqualTo(RecordingTurnStatusV2.FAILED),
                () -> assertThat(turn.errorMessage()).isEqualTo(RecordingV2StartupRecovery.LOST_TURN_MESSAGE)
        );
    }

    @Test
    void 이전_앱이_아직_처리하고_있을_수_있는_최근_턴은_그대로_둔다() {
        RecordingSessionV2 session = recordingV2DomainRepository.createSession("session", 1L, 2L, null, "hash");
        recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);

        recovery.recover(LocalDateTime.now().plusMinutes(1));

        assertThat(recordingV2DomainRepository.findTurns(session.id()))
                .extracting(RecordingTurnV2::status)
                .containsExactly(RecordingTurnStatusV2.PROCESSING);
    }

    @Test
    void 세션_유지_시간이_지난_녹음_중_세션을_끝낸_것으로_바꾼다() {
        RecordingSessionV2 old = recordingV2DomainRepository.createSession("old", 1L, 2L, null, "hash");
        LocalDateTime now = LocalDateTime.now().plusHours(13);

        recovery.recover(now);

        RecordingSessionV2 found = recordingV2DomainRepository.getSession(old.id());
        assertAll(
                () -> assertThat(found.status()).isEqualTo(RecordingSessionStatusV2.FINISHING),
                () -> assertThat(found.finishedAt()).isEqualTo(now)
        );
    }

    @Test
    void 세션_유지_시간이_지나지_않은_녹음_중_세션은_그대로_둔다() {
        RecordingSessionV2 recent = recordingV2DomainRepository.createSession("recent", 1L, 2L, null, "hash");

        recovery.recover(LocalDateTime.now().plusHours(1));

        assertThat(recordingV2DomainRepository.findSessions(RecordingSessionStatusV2.RECORDING))
                .extracting(RecordingSessionV2::id)
                .containsExactly(recent.id());
    }
}
