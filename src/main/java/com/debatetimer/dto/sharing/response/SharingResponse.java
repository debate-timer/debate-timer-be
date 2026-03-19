package com.debatetimer.dto.sharing.response;

import com.debatetimer.domain.sharing.TimerEventInfo;
import com.debatetimer.domain.sharing.TimerEventType;

public record SharingResponse(
        TimerEventType eventType,
        TimerEventInfoResponse data
) {

    public SharingResponse(TimerEventType eventType, TimerEventInfo timerEventInfo) {
        this(
                eventType,
                new TimerEventInfoResponse(timerEventInfo)
        );
    }

}
