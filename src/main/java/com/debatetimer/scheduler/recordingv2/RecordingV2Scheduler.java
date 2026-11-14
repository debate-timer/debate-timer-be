package com.debatetimer.scheduler.recordingv2;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.service.recordingv2.RecordingFinalizerV2;
import com.debatetimer.service.recordingv2.RecordingSessionRegistryV2;
import com.debatetimer.service.recordingv2.TurnProcessorV2;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 다기기 녹음 V2의 주기 작업. 기능이 꺼져 있으면 아무것도 하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class RecordingV2Scheduler {

    private static final long CLOSE_TURNS_INTERVAL_MILLIS = 1_000L;
    private static final long FINALIZE_INTERVAL_MILLIS = 5_000L;
    private static final long CLEANUP_INTERVAL_MILLIS = 60 * 60 * 1_000L;

    private final RecordingV2Properties properties;
    private final RecordingSessionRegistryV2 registry;
    private final TurnProcessorV2 turnProcessor;
    private final RecordingFinalizerV2 finalizer;
    private final AudioSpoolV2 audioSpool;

    @Scheduled(fixedDelay = CLOSE_TURNS_INTERVAL_MILLIS)
    public void closeTurns() {
        if (properties.enabled()) {
            registry.drainClosable(properties.collectWindow())
                    .forEach(turnProcessor::submit);
        }
    }

    @Scheduled(fixedDelay = FINALIZE_INTERVAL_MILLIS)
    public void finalizeSessions() {
        if (properties.enabled()) {
            finalizer.finalizeReadySessions();
        }
    }

    // 사회자가 끝내지 않고 떠난 세션의 메모리 상태와, 재시작 등으로 주인을 잃은 임시 파일을 정리한다.
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MILLIS)
    public void cleanup() {
        if (properties.enabled()) {
            registry.removeIdle(properties.sessionTtl())
                    .forEach(candidate -> audioSpool.delete(candidate.file()));
            audioSpool.deleteOlderThan(properties.sessionTtl(), Clock.systemUTC());
        }
    }
}
