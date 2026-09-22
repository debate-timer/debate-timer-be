package com.debatetimer.event.sharing;

import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.sharing.SharingRoomRegistry;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * simple broker가 청중의 룸 구독을 등록한 뒤에 후속 메시지를 보낸다.
 * SessionSubscribeEvent는 구독 등록 전에 발행될 수 있어, 그 시점에 룸으로 보내면 새 청중이 받지 못할 수 있다.
 */
@Component
public class RoomSubscribeInterceptor implements ExecutorChannelInterceptor {

    private static final String AUDIENCE_SUBSCRIBE_PREFIX = "/room/";
    private static final String CHAIRMAN_CHANNEL_PREFIX = "/chairman/";

    private final SimpMessagingTemplate messagingTemplate;
    private final SharingRoomRegistry sharingRoomRegistry;

    public RoomSubscribeInterceptor(
            @Lazy SimpMessagingTemplate messagingTemplate,
            SharingRoomRegistry sharingRoomRegistry
    ) {
        this.messagingTemplate = messagingTemplate;
        this.sharingRoomRegistry = sharingRoomRegistry;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        if (ex != null || !(handler instanceof SimpleBrokerMessageHandler)) {
            return;
        }

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        String destination = accessor.getDestination();
        if (accessor.getMessageType() != SimpMessageType.SUBSCRIBE || destination == null) {
            return;
        }

        if (destination.startsWith(CHAIRMAN_CHANNEL_PREFIX)) {
            handleChairmanSubscribe(destination);
            return;
        }

        if (destination.startsWith(AUDIENCE_SUBSCRIBE_PREFIX)) {
            handleAudienceSubscribe(destination);
        }
    }

    /**
     * 사회자의 구독은 공유 시작을 뜻하므로, 이전에 종료된 룸이라도 다시 진행 상태로 되돌린다.
     * 다른 기기에서 공유를 이어갈 수 있으므로 이전 세션의 버전 기준도 초기화한다.
     */
    private void handleChairmanSubscribe(String destination) {
        long roomId = parseRoomId(destination, CHAIRMAN_CHANNEL_PREFIX);
        sharingRoomRegistry.reopen(roomId);
        sharingRoomRegistry.resetVersion(roomId);
    }

    private void handleAudienceSubscribe(String destination) {
        long roomId = parseRoomId(destination, AUDIENCE_SUBSCRIBE_PREFIX);
        if (sharingRoomRegistry.isFinished(roomId)) {
            messagingTemplate.convertAndSend(AUDIENCE_SUBSCRIBE_PREFIX + roomId,
                    new SharingResponse(TimerEventType.FINISHED));
            return;
        }
        messagingTemplate.convertAndSend(CHAIRMAN_CHANNEL_PREFIX + roomId, new ChairmanSharingRequest(roomId));
    }

    private long parseRoomId(String destination, String prefix) {
        try {
            String parsedRoomId = destination.substring(prefix.length());
            return Long.parseLong(parsedRoomId);
        } catch (NumberFormatException exception) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_ROOM_ID);
        }
    }
}
