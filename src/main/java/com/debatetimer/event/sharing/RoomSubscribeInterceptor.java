package com.debatetimer.event.sharing;

import com.debatetimer.config.sharing.ChairmanAuthorizer;
import com.debatetimer.controller.sharing.SharingWebSocketController;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.sharing.SharingService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * 사회자 채널 구독 권한을 확인하고, simple broker가 구독을 등록한 뒤에 후속 메시지를 보낸다.
 * - 사회자 채널 구독은 활성 사회자가 되는 요청이므로, 구독이 등록되기 전(preSend)에 사회자 토큰과 테이블 소유 여부를 확인한다.
 * - SessionSubscribeEvent는 구독 등록 전에 발행될 수 있어, 그 시점에 룸으로 보내면 새 청중이 받지 못할 수 있다.
 */
@Component
public class RoomSubscribeInterceptor implements ExecutorChannelInterceptor {

    private static final String AUDIENCE_SUBSCRIBE_PREFIX = "/room/";
    private static final String CHAIRMAN_CHANNEL_PREFIX = "/chairman/";

    private final SharingService sharingService;
    private final ChairmanAuthorizer chairmanAuthorizer;

    public RoomSubscribeInterceptor(
            @Lazy SharingService sharingService,
            @Lazy ChairmanAuthorizer chairmanAuthorizer
    ) {
        this.sharingService = sharingService;
        this.chairmanAuthorizer = chairmanAuthorizer;
    }

    /**
     * 권한이 없는 사회자 채널 구독은 예외를 던져 구독 자체를 거부한다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        String destination = accessor.getDestination();
        if (accessor.getMessageType() != SimpMessageType.SUBSCRIBE || destination == null
                || !destination.startsWith(CHAIRMAN_CHANNEL_PREFIX)) {
            return message;
        }

        long roomId = parseRoomId(destination, CHAIRMAN_CHANNEL_PREFIX);
        chairmanAuthorizer.authorize(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION), roomId);
        return message;
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
            handleChairmanSubscribe(accessor, destination);
            return;
        }

        if (destination.startsWith(AUDIENCE_SUBSCRIBE_PREFIX)) {
            handleAudienceSubscribe(destination);
        }
    }

    /**
     * 사회자의 구독(preSend에서 권한 확인 완료)은 공유 시작을 뜻한다.
     * 사회자 세션 식별자가 없는 구독은 발행 권한을 얻을 수 없으므로 활성 사회자로 등록하지 않는다.
     */
    private void handleChairmanSubscribe(SimpMessageHeaderAccessor accessor, String destination) {
        long roomId = parseRoomId(destination, CHAIRMAN_CHANNEL_PREFIX);
        String chairmanSessionId = accessor.getFirstNativeHeader(SharingWebSocketController.CHAIRMAN_SESSION_HEADER);
        if (chairmanSessionId == null || chairmanSessionId.isBlank()) {
            return;
        }
        sharingService.startChairman(roomId, chairmanSessionId, accessor.getSessionId());
    }

    private void handleAudienceSubscribe(String destination) {
        long roomId = parseRoomId(destination, AUDIENCE_SUBSCRIBE_PREFIX);
        sharingService.joinAudience(roomId);
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
