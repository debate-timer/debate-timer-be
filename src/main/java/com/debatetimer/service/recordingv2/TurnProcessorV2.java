package com.debatetimer.service.recordingv2;

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
        RecordingTurnV2 turn;
        try {
            turn = createTurn(closedTurn, ranked);
        } catch (RuntimeException exception) {
            log.warn("V2 녹음 턴을 기록하지 못해 버립니다. sessionKey={}, turnKey={}", closedTurn.sessionKey(),
                    closedTurn.turnKey(), exception);
            deleteFiles(closedTurn.candidates());
            return;
        } finally {
            registry.markPersisted(closedTurn.sessionKey());
        }

        if (ranked.isEmpty()) {
            recordingV2DomainRepository.failTurn(turn.id(), "고를 수 있는 녹음 후보가 없습니다.");
            deleteFiles(closedTurn.candidates());
            return;
        }
        try {
            executor.execute(() -> process(closedTurn, turn, ranked));
        } catch (TaskRejectedException exception) {
            log.warn("V2 녹음 처리 대기열이 가득 찼습니다. sessionKey={}, turnKey={}", closedTurn.sessionKey(),
                    closedTurn.turnKey());
            recordingV2DomainRepository.failTurn(turn.id(), "처리 대기열이 가득 찼습니다.");
            deleteFiles(closedTurn.candidates());
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

    private void process(ClosedTurnV2 closedTurn, RecordingTurnV2 turn, List<AudioCandidateV2> ranked) {
        try {
            RecordingSessionV2 session = recordingV2DomainRepository.getSession(closedTurn.sessionId());
            for (AudioCandidateV2 candidate : ranked) {
                if (transcribe(session, turn, candidate)) {
                    return;
                }
            }
            recordingV2DomainRepository.failTurn(turn.id(), "모든 녹음 후보의 전사에 실패했습니다.");
        } catch (RuntimeException exception) {
            log.warn("V2 녹음 턴 처리 중 오류가 났습니다. turnId={}", turn.id(), exception);
            failQuietly(turn.id(), exception);
        } finally {
            deleteFiles(closedTurn.candidates());
        }
    }

    // 전사에 실패한 오디오도 다시 들어볼 수 있도록 전사 전에 보관한다.
    private boolean transcribe(RecordingSessionV2 session, RecordingTurnV2 turn, AudioCandidateV2 candidate) {
        Optional<String> s3Key = sttAudioStorage.upload(session.memberId(), session.sessionKey(), turn.turnKey(),
                candidate);
        try {
            List<Utterance> utterances = speechTranscriber.transcribe(candidate.file(), session.language());
            recordingV2DomainRepository.completeTurn(turn.id(), candidate.deviceId(), candidate.startedAtEpochMs(),
                    candidate.team(), s3Key.orElse(null), utterances);
            return true;
        } catch (RuntimeException exception) {
            log.warn("V2 녹음 후보 전사에 실패해 다음 후보로 넘어갑니다. turnId={}, deviceId={}, size={}", turn.id(),
                    candidate.deviceId(), candidate.sizeBytes(), exception);
            return false;
        }
    }

    private void failQuietly(long turnId, RuntimeException cause) {
        try {
            recordingV2DomainRepository.failTurn(turnId, cause.getMessage());
        } catch (RuntimeException exception) {
            log.warn("V2 녹음 턴을 실패로 기록하지 못했습니다. turnId={}", turnId, exception);
        }
    }

    private void deleteFiles(List<AudioCandidateV2> candidates) {
        candidates.forEach(candidate -> audioSpool.delete(candidate.file()));
    }

    @PreDestroy
    void close() {
        if (executor instanceof ThreadPoolTaskExecutor threadPool) {
            threadPool.shutdown();
        }
    }
}
