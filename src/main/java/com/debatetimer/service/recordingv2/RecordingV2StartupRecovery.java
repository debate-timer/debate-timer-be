package com.debatetimer.service.recordingv2;

import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 앱이 뜰 때 이전 실행에서 마무리되지 못한 V2 녹음 상태를 한 번 정리한다.
 * - 처리 작업은 메모리에만 있어 재시작하면 사라진다. 처리 제한 시간보다 오래 처리 중인 턴은 실패로 바꿔, 속기록이 최대 대기 시간까지 기다리지 않게 한다.
 *   무중단 배포 중 이전 앱이 아직 처리하는 턴을 실패로 바꾸지 않도록 오래된 턴만 바꾼다.
 * - 사회자가 끝내지 않고 떠나 세션 유지 시간이 지난 세션은 끝낸 것으로 바꿔, 전사된 턴으로 속기록을 만들게 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordingV2StartupRecovery {

    static final String LOST_TURN_MESSAGE = "서버 재시작으로 처리하지 못했습니다.";

    private final RecordingV2Properties properties;
    private final RecordingV2DomainRepository recordingV2DomainRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverOnStartup() {
        if (properties.enabled()) {
            recover(LocalDateTime.now());
        }
    }

    void recover(LocalDateTime now) {
        int failedTurns = recordingV2DomainRepository.failProcessingTurnsCreatedBefore(
                now.minus(properties.processingTimeout()), LOST_TURN_MESSAGE);
        List<RecordingSessionV2> abandoned = recordingV2DomainRepository.findSessionsCreatedBefore(
                RecordingSessionStatusV2.RECORDING, now.minus(properties.sessionTtl()));
        abandoned.forEach(session -> recordingV2DomainRepository.finishSession(session.id(), now));
        if (failedTurns > 0 || !abandoned.isEmpty()) {
            log.warn("이전 실행에서 마무리되지 못한 V2 녹음을 정리했습니다. failedTurns={}, finishedSessions={}",
                    failedTurns, abandoned.size());
        }
    }
}
