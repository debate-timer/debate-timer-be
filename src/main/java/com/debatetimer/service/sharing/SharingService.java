package com.debatetimer.service.sharing;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventDataResponse;
import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class SharingService {

    private static final String ROOM_CHANNEL_PREFIX = "/room/";

    private final SharingRoomRegistry sharingRoomRegistry;
    private final SimpMessageSendingOperations messagingTemplate;
    private final Clock clock;

    @Autowired
    public SharingService(SharingRoomRegistry sharingRoomRegistry, SimpMessageSendingOperations messagingTemplate) {
        this(sharingRoomRegistry, messagingTemplate, Clock.systemUTC());
    }

    SharingService(SharingRoomRegistry sharingRoomRegistry, SimpMessageSendingOperations messagingTemplate,
                   Clock clock) {
        this.sharingRoomRegistry = sharingRoomRegistry;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
    }

    /**
     * 사회자 이벤트를 룸 채널로 중계한다.
     * - 이미 공유한 버전 이하의 이벤트(중복·순서가 뒤바뀐 이벤트)는 중계하지 않고 룸 상태도 바꾸지 않는다.
     * - 버전 수락부터 중계까지 룸 단위로 하나씩 실행해, 낮은 버전이 높은 버전보다 늦게 반영되지 않게 한다.
     * - 청중이 수신 시점까지 흐른 시간을 보정할 수 있도록, 서버가 이벤트를 중계한 시각(epoch ms)을 함께 담는다.
     */
    public void share(long roomId, SharingRequest request) {
        TimerEvent timerEvent = request.toTimerEvent();
        sharingRoomRegistry.runExclusively(roomId, () -> {
            if (!sharingRoomRegistry.acceptVersion(roomId, request.version())) {
                return;
            }

            updateRoomStatus(roomId, timerEvent.getEventType());
            messagingTemplate.convertAndSend(ROOM_CHANNEL_PREFIX + roomId, createResponse(request, timerEvent));
        });
    }

    private SharingResponse createResponse(SharingRequest request, TimerEvent timerEvent) {
        TimerEventDataResponse data = Optional.ofNullable(timerEvent.getTimerEventData())
                .map(TimerEventDataResponse::new)
                .orElse(null);
        return new SharingResponse(request.eventType(), request.version(), clock.millis(), data);
    }

    private void updateRoomStatus(long roomId, TimerEventType eventType) {
        if (eventType == TimerEventType.FINISHED) {
            sharingRoomRegistry.markFinished(roomId);
            return;
        }
        sharingRoomRegistry.reopen(roomId);
    }
}
