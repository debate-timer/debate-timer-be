package com.debatetimer.service.recordingv2;

import static com.debatetimer.service.recordingv2.RecordingTasksV2.callQuietly;
import static com.debatetimer.service.recordingv2.RecordingTasksV2.runQuietly;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.client.recordingv2.SpeechTranscriberV2;
import com.debatetimer.client.recordingv2.SttAudioStorageV2;
import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.AudioQualityMetricsV2;
import com.debatetimer.domain.recordingv2.AudioQualityScorerV2;
import com.debatetimer.domain.recordingv2.AudioQualityScorerV2.RankedCandidate;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 수집이 끝난 턴에서 음성 품질이 가장 좋은 후보를 골라 백그라운드에서 S3에 보관하고 전사한다.
 * - 턴은 넘겨받은 스레드에서 먼저 DB에 처리 중으로 기록해, 속기록을 만들 때 빠뜨리지 않게 한다.
 *   마감한 턴은 수집 목록에서 빠지므로, DB 오류가 잠깐 나도 음성을 잃지 않도록 기록을 몇 번 다시 시도한다.
 * - 고른 후보의 전사가 실패하면 다음 후보로 넘어간다. 모두 실패하면 턴을 실패로 기록한다.
 * - 처리가 끝나면 후보 임시 파일을 모두 지운다.
 * 작업 스레드는 빈으로 등록하지 않는다(AsyncConfig 참고).
 */
@Slf4j
@Service
public class TurnProcessorV2 {

    private static final int SHUTDOWN_SECONDS = 30;
    private static final Duration CREATE_TURN_RETRY_DELAY = Duration.ofMillis(200);
    static final int CREATE_TURN_ATTEMPTS = 3;
    static final String NO_CANDIDATE = "고를 수 있는 녹음 후보가 없습니다.";
    static final String QUEUE_FULL = "처리 대기열이 가득 찼습니다.";
    static final String ALL_FAILED = "모든 녹음 후보의 전사에 실패했습니다.";

    private final RecordingV2DomainRepository recordingV2DomainRepository;
    private final RecordingSessionRegistryV2 registry;
    private final AudioSpoolV2 audioSpool;
    private final SttAudioStorageV2 sttAudioStorage;
    private final SpeechTranscriberV2 speechTranscriber;
    private final TaskExecutor executor;
    private final Duration createTurnRetryDelay;
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
                createExecutor(properties), CREATE_TURN_RETRY_DELAY);
    }

    TurnProcessorV2(
            RecordingV2DomainRepository recordingV2DomainRepository,
            RecordingSessionRegistryV2 registry,
            AudioSpoolV2 audioSpool,
            SttAudioStorageV2 sttAudioStorage,
            SpeechTranscriberV2 speechTranscriber,
            TaskExecutor executor,
            Duration createTurnRetryDelay
    ) {
        this.recordingV2DomainRepository = recordingV2DomainRepository;
        this.registry = registry;
        this.audioSpool = audioSpool;
        this.sttAudioStorage = sttAudioStorage;
        this.speechTranscriber = speechTranscriber;
        this.executor = executor;
        this.createTurnRetryDelay = createTurnRetryDelay;
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
        List<RankedCandidate> scored = scorer.rankWithScores(closedTurn.candidates());
        logRanking(closedTurn, scored);
        List<AudioCandidateV2> ranked = scored.stream()
                .map(RankedCandidate::candidate)
                .toList();
        Optional<RecordingTurnV2> created = persistTurn(closedTurn, ranked);
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

    // 저장은 됐는데 응답만 실패했을 수 있어, 다시 만들기 전에 이미 기록된 턴이 있는지 먼저 찾는다.
    private Optional<RecordingTurnV2> persistTurn(ClosedTurnV2 closedTurn, List<AudioCandidateV2> ranked) {
        for (int attempt = 1; attempt <= CREATE_TURN_ATTEMPTS; attempt++) {
            if (attempt > 1) {
                waitBeforeRetry();
                Optional<RecordingTurnV2> existing = callQuietly("기록된 턴 조회", label(closedTurn),
                        () -> recordingV2DomainRepository.findTurn(closedTurn.sessionId(), closedTurn.turnKey())
                                .orElse(null));
                if (existing.isPresent()) {
                    return existing;
                }
            }
            Optional<RecordingTurnV2> created = callQuietly("턴 기록 " + attempt + "/" + CREATE_TURN_ATTEMPTS,
                    label(closedTurn), () -> createTurn(closedTurn, ranked));
            if (created.isPresent()) {
                return created;
            }
        }
        log.error("V2 녹음 턴을 기록하지 못해 후보를 버립니다: {} candidates={}", label(closedTurn),
                closedTurn.candidates().size());
        return Optional.empty();
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(createTurnRetryDelay.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
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
            log.info("V2 녹음 턴 전사 완료: turnKey={}, deviceId={}, utterances={}, s3Key={}", turn.turnKey(),
                    candidate.deviceId(), utterances.size(), s3Key.orElse("-"));
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

    // 어떤 후보를 왜 골랐는지 확인할 수 있도록 후보마다 점수·지표를 남긴다. 순위에서 빠진 후보는 크기·길이가 맞지 않은 후보다.
    private void logRanking(ClosedTurnV2 closedTurn, List<RankedCandidate> scored) {
        String ranking = scored.stream()
                .map(ranked -> describe(ranked.candidate()) + " score=" + String.format("%.1f", ranked.score()))
                .collect(Collectors.joining(", "));
        log.info("V2 녹음 턴 후보 순위: {} candidates={} excluded={} [{}] → 선택 {}", label(closedTurn),
                closedTurn.candidates().size(), closedTurn.candidates().size() - scored.size(), ranking,
                scored.isEmpty() ? "없음" : scored.get(0).candidate().deviceId());
    }

    private String describe(AudioCandidateV2 candidate) {
        AudioQualityMetricsV2 metrics = candidate.metrics();
        String metricText = metrics.isPlausible()
                ? String.format("rms=%.1f peak=%.1f snr=%.1f silence=%.2f clip=%.3f", metrics.rmsDb(),
                metrics.peakDb(), metrics.snrDb(), metrics.silenceRatio(), metrics.clippingRatio())
                : "지표없음";
        return candidate.deviceId() + (candidate.host() ? "(host)" : "") + " " + candidate.durationMs() + "ms "
                + metricText;
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
