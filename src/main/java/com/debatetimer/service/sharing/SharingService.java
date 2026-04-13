package com.debatetimer.service.sharing;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventDataResponse;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SharingService {

    public SharingResponse share(SharingRequest request) {
        TimerEvent timerEvent = request.toTimerEvent();
        return Optional.ofNullable(timerEvent.getTimerEventData())
                .map(eventData -> new SharingResponse(
                        request.eventType(),
                        new TimerEventDataResponse(eventData)
                ))
                .orElse(new SharingResponse(request.eventType(), null));
    }
}
