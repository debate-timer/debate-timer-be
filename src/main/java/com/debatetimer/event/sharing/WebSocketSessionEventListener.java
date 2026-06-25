package com.debatetimer.event.sharing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Component
public class WebSocketSessionEventListener {

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        log.info("웹소켓 연결 요청: sessionId={}", resolveSessionId(event.getMessage()));
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        log.info("웹소켓 연결 해제: sessionId={}, status={}", event.getSessionId(), event.getCloseStatus());
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        log.info("웹소켓 구독 해제: sessionId={}", resolveSessionId(event.getMessage()));
    }

    private String resolveSessionId(Message<byte[]> message) {
        return StompHeaderAccessor.wrap(message).getSessionId();
    }
}
