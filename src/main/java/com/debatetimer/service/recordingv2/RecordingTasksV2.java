package com.debatetimer.service.recordingv2;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * V2 녹음의 백그라운드·주기 작업을 실패해도 멈추지 않게 실행한다.
 * 실패하면 예외를 밖으로 던지지 않고, 작업 이름·대상과 원인 예외를 warn 로그 한 줄로 남긴다.
 * 한 턴·세션의 실패가 같은 주기에 처리하는 다른 턴·세션을 막지 않게 하려고 쓴다.
 */
@Slf4j
public final class RecordingTasksV2 {

    private RecordingTasksV2() {
    }

    /**
     * @return 작업이 끝까지 실행됐으면 true
     */
    public static boolean runQuietly(String action, String target, Runnable task) {
        return callQuietly(action, target, () -> {
            task.run();
            return true;
        }).orElse(false);
    }

    /**
     * @return 작업 결과. 실패하면 비어 있다.
     */
    public static <T> Optional<T> callQuietly(String action, String target, Supplier<T> task) {
        try {
            return Optional.ofNullable(task.get());
        } catch (RuntimeException exception) {
            log.warn("V2 녹음 {} 실패: {}", action, target, exception);
            return Optional.empty();
        }
    }
}
