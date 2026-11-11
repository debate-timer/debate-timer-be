package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.client.recordingv2.SpeechTranscriberV2;
import com.debatetimer.client.recordingv2.SttAudioStorageV2;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.AudioQualityMetricsV2;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

class TurnProcessorV2Test {

    private static final long SESSION_ID = 1L;
    private static final long TURN_ID = 10L;
    private static final RecordingSessionV2 SESSION = new RecordingSessionV2(SESSION_ID, "session", 7L, 2L, "ko-KR",
            "hash", RecordingSessionStatusV2.RECORDING, null, null, null, LocalDateTime.now());
    private static final RecordingTurnV2 TURN = new RecordingTurnV2(TURN_ID, SESSION_ID, "0-1", 0, null, 0L,
            RecordingTurnStatusV2.PROCESSING, 2, null, null, List.of(), null);

    @TempDir
    private Path directory;

    private RecordingV2DomainRepository repository;
    private RecordingSessionRegistryV2 registry;
    private AudioSpoolV2 audioSpool;
    private SttAudioStorageV2 storage;
    private SpeechTranscriberV2 transcriber;

    @BeforeEach
    void setUp() {
        repository = mock(RecordingV2DomainRepository.class);
        registry = mock(RecordingSessionRegistryV2.class);
        audioSpool = new AudioSpoolV2(directory, 1_000_000);
        storage = mock(SttAudioStorageV2.class);
        transcriber = mock(SpeechTranscriberV2.class);
        when(repository.createTurn(anyLong(), anyString(), anyInt(), any(), anyLong(), anyInt())).thenReturn(TURN);
        when(repository.getSession(SESSION_ID)).thenReturn(SESSION);
        when(storage.upload(anyLong(), anyString(), anyString(), any())).thenReturn(Optional.of("s3-key"));
    }

    @Test
    void 가장_좋은_후보만_보관하고_전사한_뒤_후보_파일을_모두_지운다() throws IOException {
        AudioCandidateV2 noisy = candidate("noisy", false, 5, Stance.PROS, 1_000L);
        AudioCandidateV2 clear = candidate("clear", false, 35, Stance.PROS, 1_200L);
        List<Utterance> utterances = List.of(new Utterance(0, "찬성 입론입니다."));
        when(transcriber.transcribe(clear.file(), "ko-KR")).thenReturn(utterances);

        processor(new SyncTaskExecutor()).submit(new ClosedTurnV2(SESSION_ID, "session", "0-1", List.of(noisy, clear)));

        verify(registry).markPersisted("session");
        verify(storage).upload(7L, "session", "0-1", clear);
        verify(storage, never()).upload(7L, "session", "0-1", noisy);
        verify(repository).completeTurn(TURN_ID, "clear", 1_200L, Stance.PROS, "s3-key", utterances);
        assertThat(noisy.file()).doesNotExist();
        assertThat(clear.file()).doesNotExist();
    }

    @Test
    void 턴_정보는_사회자_기기_값을_우선한다() throws IOException {
        AudioCandidateV2 audience = candidate("audience", false, 35, Stance.PROS, 1_000L);
        AudioCandidateV2 host = candidate("host", true, 5, Stance.CONS, 2_000L);
        when(transcriber.transcribe(any(), any())).thenReturn(List.of());

        processor(new SyncTaskExecutor()).submit(new ClosedTurnV2(SESSION_ID, "session", "0-1",
                List.of(audience, host)));

        verify(repository).createTurn(SESSION_ID, "0-1", 0, Stance.CONS, 2_000L, 2);
    }

    @Test
    void 고른_후보의_전사가_실패하면_다음_후보로_전사한다() throws IOException {
        AudioCandidateV2 best = candidate("best", false, 35, null, 1_000L);
        AudioCandidateV2 next = candidate("next", false, 5, null, 1_100L);
        when(transcriber.transcribe(best.file(), "ko-KR"))
                .thenThrow(new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED));
        when(transcriber.transcribe(next.file(), "ko-KR")).thenReturn(List.of());

        processor(new SyncTaskExecutor()).submit(new ClosedTurnV2(SESSION_ID, "session", "0-1", List.of(best, next)));

        verify(repository).completeTurn(TURN_ID, "next", 1_100L, null, "s3-key", List.of());
    }

    @Test
    void 모든_후보의_전사가_실패하면_턴을_실패로_기록한다() throws IOException {
        AudioCandidateV2 only = candidate("only", true, 35, null, 1_000L);
        when(transcriber.transcribe(any(), any()))
                .thenThrow(new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED));

        processor(new SyncTaskExecutor()).submit(new ClosedTurnV2(SESSION_ID, "session", "0-1", List.of(only)));

        verify(repository).failTurn(eq(TURN_ID), anyString());
        assertThat(only.file()).doesNotExist();
    }

    @Test
    void 고를_수_있는_후보가_없으면_전사하지_않고_실패로_기록한다() throws IOException {
        Path file = Files.write(directory.resolve("broken.webm"), new byte[]{1});
        AudioCandidateV2 broken = new AudioCandidateV2("broken", true, file, "audio/webm", null, 1, 60_000, 0, 0,
                null, AudioQualityMetricsV2.empty(), Instant.EPOCH);

        processor(new SyncTaskExecutor()).submit(new ClosedTurnV2(SESSION_ID, "session", "0-1", List.of(broken)));

        verify(repository).failTurn(eq(TURN_ID), anyString());
        verify(transcriber, never()).transcribe(any(), any());
        assertThat(file).doesNotExist();
    }

    @Test
    void 처리_대기열이_가득_차면_실패로_기록하고_파일을_지운다() throws IOException {
        AudioCandidateV2 only = candidate("only", true, 35, null, 1_000L);
        TaskExecutor rejecting = task -> {
            throw new TaskRejectedException("full");
        };

        processor(rejecting).submit(new ClosedTurnV2(SESSION_ID, "session", "0-1", List.of(only)));

        verify(repository).failTurn(eq(TURN_ID), anyString());
        assertThat(only.file()).doesNotExist();
    }

    private TurnProcessorV2 processor(TaskExecutor executor) {
        return new TurnProcessorV2(repository, registry, audioSpool, storage, transcriber, executor);
    }

    private AudioCandidateV2 candidate(String deviceId, boolean host, double snrDb, Stance team, long startedAt)
            throws IOException {
        Path file = Files.write(directory.resolve(deviceId + ".webm"), new byte[40_000]);
        return new AudioCandidateV2(deviceId, host, file, "audio/webm", null, 40_000, 10_000, startedAt, 0, team,
                new AudioQualityMetricsV2(-20.0, -3.0, 0.0, 0.2, snrDb), Instant.EPOCH);
    }
}
