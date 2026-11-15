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
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

class RecordingV2DomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private RecordingV2DomainRepository recordingV2DomainRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("기다리는 작업이 끝나지 않았습니다.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

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
        void 세션과_턴_키로_기록된_턴을_찾는다() {
            RecordingSessionV2 session = recordingV2DomainRepository.createSession("session-key", 1L, 2L, null,
                    "hash");
            RecordingTurnV2 created = recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);

            assertAll(
                    () -> assertThat(recordingV2DomainRepository.findTurn(session.id(), "0-1"))
                            .map(RecordingTurnV2::id)
                            .contains(created.id()),
                    () -> assertThat(recordingV2DomainRepository.findTurn(session.id(), "1-2")).isEmpty()
            );
        }

        @Test
        void 오래된_처리_중_턴을_실패로_바꾸는_사이_전사가_끝나도_완료_결과를_덮어쓰지_않는다() throws Exception {
            RecordingSessionV2 session = recordingV2DomainRepository.createSession("session-key", 1L, 2L, null,
                    "hash");
            RecordingTurnV2 turn = recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);
            CountDownLatch recoveryLocked = new CountDownLatch(1);
            CountDownLatch releaseRecovery = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> recovery = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                    recordingV2DomainRepository.failProcessingTurnsCreatedBefore(LocalDateTime.now().plusMinutes(1),
                            "서버 재시작으로 처리하지 못했습니다.");
                    recoveryLocked.countDown();
                    await(releaseRecovery);
                }));
                await(recoveryLocked);
                Future<?> completion = executor.submit(() -> recordingV2DomainRepository.completeTurn(turn.id(),
                        "device", 0L, null, null, List.of()));
                // 잠금이 없으면 이 사이에 완료가 먼저 커밋되고, 복구가 커밋하며 실패로 덮어쓴다.
                Thread.sleep(300);
                releaseRecovery.countDown();
                recovery.get(5, TimeUnit.SECONDS);
                completion.get(5, TimeUnit.SECONDS);
            } finally {
                releaseRecovery.countDown();
                executor.shutdownNow();
            }

            RecordingTurnV2 found = recordingV2DomainRepository.findTurns(session.id()).get(0);
            assertAll(
                    () -> assertThat(found.status()).isEqualTo(RecordingTurnStatusV2.DONE),
                    () -> assertThat(found.errorMessage()).isNull()
            );
        }

        @Test
        void 없는_턴을_완료하려_하면_서버_오류를_던진다() {
            assertThatThrownBy(() -> recordingV2DomainRepository.completeTurn(999L, "device", 0L, null, null,
                    List.of()))
                    .isInstanceOf(DTServerErrorException.class)
                    .hasMessage(ServerErrorCode.RECORDING_TURN_NOT_FOUND.getMessage());
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
