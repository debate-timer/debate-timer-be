package com.debatetimer.event.sharing;

import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomSubscribeListener {

    private static final String AUDIENCE_SUBSCRIBE_PREFIX = "/room/";
    private static final String CHAIRMAN_CHANNEL_PREFIX = "/chairman/";

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        if (destination.startsWith(AUDIENCE_SUBSCRIBE_PREFIX)) {
            long roomId = parseRoomId(destination);
            log.info("관객 구독 발생: roomId={}, sessionId={}", roomId, accessor.getSessionId());
            messagingTemplate.convertAndSend(CHAIRMAN_CHANNEL_PREFIX + roomId, new ChairmanSharingRequest(roomId));
            log.info("사회자 구독 알림 발행: roomId={}", roomId);
        }
    }

    private long parseRoomId(String destination) {
        try {
            String parsedRoomId = destination.substring(AUDIENCE_SUBSCRIBE_PREFIX.length());
            return Long.parseLong(parsedRoomId);
        } catch (NumberFormatException exception) {
            log.warn("유효하지 않은 roomId 구독 시도: destination={}", destination);
            throw new DTClientErrorException(ClientErrorCode.INVALID_ROOM_ID);
        }
    }
}

