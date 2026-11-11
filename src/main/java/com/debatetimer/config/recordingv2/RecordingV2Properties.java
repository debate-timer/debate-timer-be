package com.debatetimer.config.recordingv2;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 다기기 녹음 V2 설정. 꺼져 있으면 V2 API는 404로 응답하고 스케줄러도 아무것도 하지 않는다.
 * 기본값은 1GB 메모리 인스턴스를 기준으로 잡는다.
 */
@ConfigurationProperties(prefix = "recording-v2")
public record RecordingV2Properties(
        Boolean enabled,
        // 첫 후보가 도착한 뒤 다른 기기의 후보를 기다리는 시간
        Duration collectWindow,
        // 사회자 기기를 포함해 한 세션에 참가할 수 있는 기기 수
        Integer maxDevices,
        // 후보 오디오를 받아 두는 로컬 디렉터리
        String spoolDir,
        // 스풀 디렉터리에 받아 둘 수 있는 오디오 bytes 합계 상한
        Long maxSpoolBytes,
        // 고른 후보를 S3에 올리고 전사하는 스레드 수. V2 전용 Soniox 동시 전사 수와 같다.
        Integer workers,
        Integer queueCapacity,
        // 사회자가 끝낸 뒤 남은 턴 처리를 기다리는 최대 시간. 지나면 끝난 턴만으로 속기록을 만든다.
        Duration finishTimeout,
        // 녹음 중인 세션의 기기·수집 상태를 메모리에 두는 최대 시간
        Duration sessionTtl
) {

    private static final Duration DEFAULT_COLLECT_WINDOW = Duration.ofSeconds(20);
    private static final int DEFAULT_MAX_DEVICES = 8;
    private static final String DEFAULT_SPOOL_DIR = Path.of(System.getProperty("java.io.tmpdir"), "recording-v2")
            .toString();
    private static final long DEFAULT_MAX_SPOOL_BYTES = 512L * 1024 * 1024;
    private static final int DEFAULT_WORKERS = 2;
    private static final int DEFAULT_QUEUE_CAPACITY = 50;
    private static final Duration DEFAULT_FINISH_TIMEOUT = Duration.ofMinutes(10);
    private static final Duration DEFAULT_SESSION_TTL = Duration.ofHours(12);

    public RecordingV2Properties {
        enabled = enabled != null && enabled;
        collectWindow = collectWindow == null ? DEFAULT_COLLECT_WINDOW : collectWindow;
        maxDevices = maxDevices == null ? DEFAULT_MAX_DEVICES : maxDevices;
        spoolDir = spoolDir == null || spoolDir.isBlank() ? DEFAULT_SPOOL_DIR : spoolDir;
        maxSpoolBytes = maxSpoolBytes == null ? DEFAULT_MAX_SPOOL_BYTES : maxSpoolBytes;
        workers = workers == null ? DEFAULT_WORKERS : workers;
        queueCapacity = queueCapacity == null ? DEFAULT_QUEUE_CAPACITY : queueCapacity;
        finishTimeout = finishTimeout == null ? DEFAULT_FINISH_TIMEOUT : finishTimeout;
        sessionTtl = sessionTtl == null ? DEFAULT_SESSION_TTL : sessionTtl;
    }
}
