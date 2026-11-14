package com.debatetimer.service.recordingv2;

import static com.debatetimer.service.recordingv2.RecordingTasksV2.callQuietly;
import static com.debatetimer.service.recordingv2.RecordingTasksV2.runQuietly;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.client.recordingv2.SpeechTranscriberV2;
import com.debatetimer.client.recordingv2.SttAudioStorageV2;
import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.AudioQualityScorerV2;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 수집이 끝난 턴에서 음성 품질이 가장 좋은 후보를 골라 백그라운드에서 S3에 보관하고 전사한다.
 * - 턴은 넘겨받은 스레드에서 먼저 DB에 처리 중으로 기록해, 속기록을 만들 때 빠뜨리지 않게 한다.
 * - 고른 후보의 전사가 실패하면 다음 후보로 넘어간다. 모두 실패하면 턴을 실패로 기록한다.
 * - 처리가 끝나면 후보 임시 파일을 모두 지운다.
 * 작업 스레드는 빈으로 등록하지 않는다(AsyncConfig 참고).
 */
@Slf4j
@Service
public class TurnProcessorV2 {

    private static final int SHUTDOWN_SECONDS = 30;
    static final String NO_CANDIDATE = "고를 수 있는 녹음 후보가 없습니다.";
    static final String QUEUE_FULL = "처리 대기열이 가득 찼습니다.";
    static final String ALL_FAILED = "모든 녹음 후보의 전사에 실패했습니다.";

    private final RecordingV2DomainRepository recordingV2DomainRepository;
    private final RecordingSessionRegistryV2 registry;
    private final AudioSpoolV2 audioSpool;
    private final SttAudioStorageV2 sttAudioStorage;
    private final SpeechTranscriberV2 speechTranscriber;
    private final TaskExecutor executor;
    private final AudioQualityScorerV2 scorer = new AudioQualityScorerV2();

    @Autowired
    public TurnProcessorV2(
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            AudioSpoolV2 audioSpool,
            SttAudioStorageV2 sttAudioStorage,
            SpeechTranscriberV2 speechTranscriber,
            RecordingV2Properties properties
    ) {
        this(recordingV2DomainRepository, registry, audioSpool, sttAudioStorage, speechTranscriber,
                createExecutor(properties));
    }

    TurnProcessorV2(
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            AudioSpoolV2 audioSpool,
            SttAudioStorageV2 sttAudioStorage,
            SpeechTranscriberV2 speechTranscriber,
            TaskExecutor executor
    ) {
        this.recordingV2DomainRepository = recordingV2DomainRepository;
        this.registry = registry;
        this.audioSpool = audioSpool;
        this.sttAudioStorage = sttAudioStorage;
        this.speechTranscriber = speechTranscriber;
        this.executor = executor;
    }

    private static ThreadPoolTaskExecutor createExecutor(RecordingV2Properties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.workers());
        executor.setMaxPoolSize(properties.workers());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setThreadNamePrefix("recording-v2-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(SHUTDOWN_SECONDS);
        executor.initialize();
        return executor;
    }

    public void submit(ClosedTurnV2 closedTurn) {
        List<AudioCandidateV2> ranked = scorer.rank(closedTurn.candidates());
        Optional<RecordingTurnV2> created = callQuietly("턴 기록", label(closedTurn),
                () -> createTurn(closedTurn, ranked));
        registry.markPersisted(closedTurn.sessionKey());
        if (created.isEmpty()) {
            deleteFiles(closedTurn);
            return;
        }
        RecordingTurnV2 turn = created.get();
        if (ranked.isEmpty()) {
            fail(turn, closedTurn, NO_CANDIDATE);
            return;
        }
        if (!dispatch(closedTurn, turn, ranked)) {
            fail(turn, closedTurn, QUEUE_FULL);
        }
    }

    // 고르기 전이라도 턴 정보는 사회자 기기 값을 우선하고, 없으면 가장 좋은 후보 값을 쓴다.
    private RecordingTurnV2 createTurn(ClosedTurnV2 closedTurn, List<AudioCandidateV2> ranked) {
        AudioCandidateV2 representative = closedTurn.candidates().stream()
                .filter(AudioCandidateV2::host)
                .findFirst()
                .orElse(ranked.isEmpty() ? closedTurn.candidates().get(0) : ranked.get(0));
        return recordingV2DomainRepository.createTurn(closedTurn.sessionId(), closedTurn.turnKey(),
                representative.timeBoxIndex(), representative.team(), representative.startedAtEpochMs(),
                closedTurn.candidates().size());
    }

    private boolean dispatch(ClosedTurnV2 closedTurn, RecordingTurnV2 turn, List<AudioCandidateV2> ranked) {
        try {
            executor.execute(() -> process(closedTurn, turn, ranked));
            return true;
        } catch (TaskRejectedException exception) {
            log.warn("V2 녹음 처리 대기열이 가득 찼습니다: {}", label(closedTurn));
            return false;
        }
    }

    // 순위대로 전사해 처음 성공한 후보를 쓴다.
    private void process(ClosedTurnV2 closedTurn, RecordingTurnV2 turn, List<AudioCandidateV2> ranked) {
        boolean done = callQuietly("턴 처리", label(closedTurn), () -> {
            RecordingSessionV2 session = recordingV2DomainRepository.getSession(closedTurn.sessionId());
            return ranked.stream().anyMatch(candidate -> transcribe(session, turn, candidate));
        }).orElse(false);
        if (done) {
            deleteFiles(closedTurn);
            return;
        }
        fail(turn, closedTurn, ALL_FAILED);
    }

    // 전사에 실패한 오디오도 다시 들어볼 수 있도록 전사 전에 보관한다.
    private boolean transcribe(RecordingSessionV2 session, RecordingTurnV2 turn, AudioCandidateV2 candidate) {
        Optional<String> s3Key = sttAudioStorage.upload(session.memberId(), session.sessionKey(), turn.turnKey(),
                candidate);
        return runQuietly("후보 전사", turn.id() + "/" + candidate.deviceId(), () -> {
            List<Utterance> utterances = speechTranscriber.transcribe(candidate.file(), session.language());
            recordingV2DomainRepository.completeTurn(turn.id(), candidate.deviceId(), candidate.startedAtEpochMs(),
                    candidate.team(), s3Key.orElse(null), utterances);
        });
    }

    // 실패 기록이 실패해도 임시 파일은 지운다.
    private void fail(RecordingTurnV2 turn, ClosedTurnV2 closedTurn, String reason) {
        runQuietly("턴 실패 기록", label(closedTurn), () -> recordingV2DomainRepository.failTurn(turn.id(), reason));
        deleteFiles(closedTurn);
    }

    private void deleteFiles(ClosedTurnV2 closedTurn) {
        closedTurn.candidates().forEach(candidate -> audioSpool.delete(candidate.file()));
    }

    private String label(ClosedTurnV2 closedTurn) {
        return closedTurn.sessionKey() + "/" + closedTurn.turnKey();
    }

    @PreDestroy
    void close() {
        if (executor instanceof ThreadPoolTaskExecutor threadPool) {
            threadPool.shutdown();
        }
    }
}
