package com.debatetimer.event.sharing;

import com.debatetimer.service.sharing.SharingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 사회자의 연결이 끊기면 활성 사회자 등록을 해제해, 이후 입장한 청중이 사회자 부재를 바로 알 수 있게 한다.
 */
@Component
@RequiredArgsConstructor
public class ChairmanDisconnectListener {

    private final SharingService sharingService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        sharingService.leave(event.getSessionId());
    }
}
