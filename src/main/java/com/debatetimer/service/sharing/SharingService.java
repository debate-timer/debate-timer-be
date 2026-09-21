package com.debatetimer.service.sharing;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventDataResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharingService {

    private final SharingRoomRegistry sharingRoomRegistry;

    public SharingResponse share(long roomId, SharingRequest request) {
        TimerEvent timerEvent = request.toTimerEvent();
        updateRoomStatus(roomId, timerEvent.getEventType());
        TimerEventDataResponse data = Optional.ofNullable(timerEvent.getTimerEventData())
                .map(TimerEventDataResponse::new)
                .orElse(null);
        return new SharingResponse(request.eventType(), request.version(), data);
    }

    private void updateRoomStatus(long roomId, TimerEventType eventType) {
        if (eventType == TimerEventType.FINISHED) {
            sharingRoomRegistry.markFinished(roomId);
            return;
        }
        sharingRoomRegistry.reopen(roomId);
    }
}
