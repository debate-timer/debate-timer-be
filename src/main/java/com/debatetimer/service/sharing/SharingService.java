package com.debatetimer.service.sharing;

import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventInfoResponse;
import org.springframework.stereotype.Service;

@Service
public class SharingService {

    public SharingResponse share(SharingRequest request) {
        if (!request.hasEventData()) {
            return new SharingResponse(request.eventType(), null);
        }

        return new SharingResponse(
                request.eventType(),
                new TimerEventInfoResponse(request.toTimerEventInfo())
        );
    }
}
