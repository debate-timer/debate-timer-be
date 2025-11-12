package com.debatetimer.event.sharing;

import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

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
            long roomId = Long.parseLong(destination.replace(AUDIENCE_SUBSCRIBE_PREFIX, ""));
            messagingTemplate.convertAndSend(CHAIRMAN_CHANNEL_PREFIX + roomId, new ChairmanSharingRequest(roomId));
        }
    }
}

