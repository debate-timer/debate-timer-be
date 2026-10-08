package com.debatetimer.event.sharing;

import com.debatetimer.client.notifier.SharingNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 공유 종료 기록이 저장된 뒤에 공유 통계를 알린다.
 * 종료 이벤트는 기록을 저장한 트랜잭션이 끝난 뒤 발행되므로, 트랜잭션 밖에서 발행돼도 알린다.
 * 알림은 공유 중계와 무관하므로 별도 스레드에서 보내고, 실패해도 기록만 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SharingFinishedListener {

    private final SharingNotifier sharingNotifier;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleSharingFinished(SharingFinishedEvent event) {
        try {
            sharingNotifier.sendSharingFinished(event);
        } catch (RuntimeException exception) {
            log.warn("공유 종료 알림 발송에 실패했습니다. sharingLogId={}", event.sharingLogId(), exception);
        }
    }
}
