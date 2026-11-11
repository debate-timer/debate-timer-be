package com.debatetimer.domainrepository.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RecordingV2DomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private RecordingV2DomainRepository recordingV2DomainRepository;

    @Nested
    class Session {

        @Test
        void 녹음_세션을_녹음_중_상태로_저장한다() {
            RecordingSessionV2 created = recordingV2DomainRepository.createSession("session-key", 1L, 2L, "ko-KR",
                    "hash");

            RecordingSessionV2 found = recordingV2DomainRepository.getSession("session-key");
            assertAll(
                    () -> assertThat(found.id()).isEqualTo(created.id()),
                    () -> assertThat(found.memberId()).isEqualTo(1L),
                    () -> assertThat(found.tableId()).isEqualTo(2L),
                    () -> assertThat(found.language()).isEqualTo("ko-KR"),
                    () -> assertThat(found.joinCodeHash()).isEqualTo("hash"),
                    () -> assertThat(found.status()).isEqualTo(RecordingSessionStatusV2.RECORDING),
                    () -> assertThat(found.transcriptId()).isNull()
            );
        }

        @Test
        void 없는_세션이면_예외를_던진다() {
            assertThatThrownBy(() -> recordingV2DomainRepository.getSession("missing"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_SESSION_NOT_FOUND.getMessage());
        }

        @Test
        void 종료한_세션은_종료_중_상태로_찾을_수_있고_완료하면_속기록을_연결한다() {
            RecordingSessionV2 created = recordingV2DomainRepository.createSession("session-key", 1L, 2L, null,
                    "hash");

            recordingV2DomainRepository.finishSession(created.id(), LocalDateTime.of(2026, 11, 12, 10, 0));
            assertThat(recordingV2DomainRepository.findSessions(RecordingSessionStatusV2.FINISHING))
                    .extracting(RecordingSessionV2::id)
                    .containsExactly(created.id());

            recordingV2DomainRepository.completeSession(created.id(), 10L);
            RecordingSessionV2 found = recordingV2DomainRepository.getSession(created.id());
            assertAll(
                    () -> assertThat(found.status()).isEqualTo(RecordingSessionStatusV2.COMPLETED),
                    () -> assertThat(found.transcriptId()).isEqualTo(10L),
                    () -> assertThat(found.finishedAt()).isEqualTo(LocalDateTime.of(2026, 11, 12, 10, 0))
            );
        }
    }

    @Nested
    class Turn {

        @Test
        void 턴을_처리_중으로_저장하고_완료하면_전사_결과를_보관한다() {
            RecordingSessionV2 session = recordingV2DomainRepository.createSession("session-key", 1L, 2L, null,
                    "hash");
            RecordingTurnV2 created = recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, Stance.PROS,
                    1_000L, 3);
            List<Utterance> utterances = List.of(new Utterance(100, "찬성 입론입니다."));

            recordingV2DomainRepository.completeTurn(created.id(), "device", 1_500L, Stance.CONS, "key.webm",
                    utterances);

            RecordingTurnV2 found = recordingV2DomainRepository.findTurns(session.id()).get(0);
            assertAll(
                    () -> assertThat(created.status()).isEqualTo(RecordingTurnStatusV2.PROCESSING),
                    () -> assertThat(found.status()).isEqualTo(RecordingTurnStatusV2.DONE),
                    () -> assertThat(found.candidateCount()).isEqualTo(3),
                    () -> assertThat(found.selectedDeviceId()).isEqualTo("device"),
                    () -> assertThat(found.startedAtEpochMs()).isEqualTo(1_500L),
                    () -> assertThat(found.team()).isEqualTo(Stance.CONS),
                    () -> assertThat(found.s3Key()).isEqualTo("key.webm"),
                    () -> assertThat(found.utterances()).isEqualTo(utterances),
                    () -> assertThat(recordingV2DomainRepository.existsTurn(session.id(), "0-1")).isTrue()
            );
        }

        @Test
        void 실패한_턴은_긴_오류_메시지를_잘라_보관한다() {
            RecordingSessionV2 session = recordingV2DomainRepository.createSession("session-key", 1L, 2L, null,
                    "hash");
            RecordingTurnV2 created = recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);

            recordingV2DomainRepository.failTurn(created.id(), "x".repeat(300));

            RecordingTurnV2 found = recordingV2DomainRepository.findTurns(session.id()).get(0);
            assertAll(
                    () -> assertThat(found.status()).isEqualTo(RecordingTurnStatusV2.FAILED),
                    () -> assertThat(found.errorMessage()).hasSize(255),
                    () -> assertThat(found.utterances()).isEmpty()
            );
        }
    }
}
