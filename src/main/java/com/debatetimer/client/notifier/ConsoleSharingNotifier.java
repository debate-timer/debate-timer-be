package com.debatetimer.client.notifier;

import com.debatetimer.event.sharing.SharingFinishedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleSharingNotifier implements SharingNotifier {

    @Override
    public void sendSharingFinished(SharingFinishedEvent event) {
        log.info("공유 종료 정보가 채널로 발송되었습니다\n{}", SharingFinishedMessage.from(event));
    }
}
