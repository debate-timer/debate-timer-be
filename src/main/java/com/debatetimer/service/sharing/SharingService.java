package com.debatetimer.service.sharing;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
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
    private static final String CHAIRMAN_CHANNEL_PREFIX = "/chairman/";

    private final SharingRoomRegistry sharingRoomRegistry;
    private final ChairmanSessionRegistry chairmanSessionRegistry;
    private final SimpMessageSendingOperations messagingTemplate;
    private final Clock clock;

    @Autowired
    public SharingService(
            SharingRoomRegistry sharingRoomRegistry,
            ChairmanSessionRegistry chairmanSessionRegistry,
            SimpMessageSendingOperations messagingTemplate
    ) {
        this(sharingRoomRegistry, chairmanSessionRegistry, messagingTemplate, Clock.systemUTC());
    }

    SharingService(
            SharingRoomRegistry sharingRoomRegistry,
            ChairmanSessionRegistry chairmanSessionRegistry,
            SimpMessageSendingOperations messagingTemplate,
            Clock clock
    ) {
        this.sharingRoomRegistry = sharingRoomRegistry;
        this.chairmanSessionRegistry = chairmanSessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
    }

    /**
     * 사회자 이벤트를 룸 채널로 중계한다.
     * - 룸의 활성 사회자 세션이 보낸 이벤트만 중계한다. 밀려난 세션의 이벤트는 버리고 밀려났음을 다시 알린다.
     * - 이미 공유한 버전 이하의 이벤트(중복·순서가 뒤바뀐 이벤트)는 중계하지 않고 룸 상태도 바꾸지 않는다.
     * - 버전 수락부터 중계까지 룸 단위로 하나씩 실행해, 낮은 버전이 높은 버전보다 늦게 반영되지 않게 한다.
     * - 청중이 수신 시점까지 흐른 시간을 보정할 수 있도록, 서버가 이벤트를 중계한 시각(epoch ms)을 함께 담는다.
     */
    public void share(long roomId, String chairmanSessionId, String simpSessionId, SharingRequest request) {
        TimerEvent timerEvent = request.toTimerEvent();
        sharingRoomRegistry.runExclusively(roomId, () -> {
            if (!claimChairman(roomId, chairmanSessionId, simpSessionId).isAccepted()) {
                return;
            }
            if (!sharingRoomRegistry.acceptVersion(roomId, request.version())) {
                return;
            }

            updateRoomStatus(roomId, timerEvent.getEventType());
            messagingTemplate.convertAndSend(ROOM_CHANNEL_PREFIX + roomId, createResponse(request, timerEvent));
        });
    }

    /**
     * 사회자의 구독은 공유 시작을 뜻하므로, 최신 사회자로 등록하고 이전에 종료된 룸이라도 다시 진행 상태로 되돌린다.
     */
    public void startChairman(long roomId, String chairmanSessionId, String simpSessionId) {
        sharingRoomRegistry.runExclusively(roomId, () -> {
            if (claimChairman(roomId, chairmanSessionId, simpSessionId).isAccepted()) {
                sharingRoomRegistry.reopen(roomId);
            }
        });
    }

    /**
     * 새 청중에게 보여줄 상태를 준비한다.
     * - 종료된 룸이면 청중에게 종료를 알린다.
     * - 활성 사회자가 없으면 청중에게 사회자 부재를 알린다.
     * - 그 외에는 사회자에게 현재 상태 공유를 요청한다.
     */
    public void joinAudience(long roomId) {
        if (sharingRoomRegistry.isFinished(roomId)) {
            messagingTemplate.convertAndSend(ROOM_CHANNEL_PREFIX + roomId, new SharingResponse(TimerEventType.FINISHED));
            return;
        }
        if (!chairmanSessionRegistry.hasActiveChairman(roomId)) {
            messagingTemplate.convertAndSend(ROOM_CHANNEL_PREFIX + roomId,
                    new SharingResponse(TimerEventType.CHAIRMAN_ABSENT));
            return;
        }
        messagingTemplate.convertAndSend(CHAIRMAN_CHANNEL_PREFIX + roomId, ChairmanSharingRequest.syncRequest(roomId));
    }

    public void leave(String simpSessionId) {
        chairmanSessionRegistry.release(simpSessionId);
    }

    /**
     * 새 사회자가 되면 다른 기기(시계)에서 발행할 수 있으므로 버전 기준을 초기화하고,
     * 밀려난 사회자가 공유를 멈출 수 있도록 사회자 채널로 현재 활성 사회자를 알린다.
     */
    private ChairmanClaim claimChairman(long roomId, String chairmanSessionId, String simpSessionId) {
        ChairmanClaim claim = chairmanSessionRegistry.claim(roomId, chairmanSessionId, simpSessionId);
        if (claim.isNewChairman()) {
            sharingRoomRegistry.resetVersion(roomId);
        }
        if (claim == ChairmanClaim.TAKEOVER || claim == ChairmanClaim.REJECTED) {
            notifyActiveChairman(roomId);
        }
        return claim;
    }

    private void notifyActiveChairman(long roomId) {
        chairmanSessionRegistry.findLatestSessionId(roomId)
                .ifPresent(activeSessionId -> messagingTemplate.convertAndSend(
                        CHAIRMAN_CHANNEL_PREFIX + roomId,
                        ChairmanSharingRequest.replaced(roomId, activeSessionId)
                ));
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
