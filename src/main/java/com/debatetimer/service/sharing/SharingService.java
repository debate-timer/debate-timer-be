package com.debatetimer.service.sharing;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventDataResponse;
import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SharingService {

    private final SharingRoomRegistry sharingRoomRegistry;
    private final Clock clock;

    @Autowired
    public SharingService(SharingRoomRegistry sharingRoomRegistry) {
        this(sharingRoomRegistry, Clock.systemUTC());
    }

    SharingService(SharingRoomRegistry sharingRoomRegistry, Clock clock) {
        this.sharingRoomRegistry = sharingRoomRegistry;
        this.clock = clock;
    }

    /**
     * 청중이 수신 시점까지 흐른 시간을 보정할 수 있도록, 서버가 이벤트를 중계한 시각(epoch ms)을 함께 담는다.
     */
    public SharingResponse share(long roomId, SharingRequest request) {
        TimerEvent timerEvent = request.toTimerEvent();
        updateRoomStatus(roomId, timerEvent.getEventType());
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
