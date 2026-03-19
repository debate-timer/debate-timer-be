package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.TimerEventInfo;
import com.debatetimer.domain.sharing.TimerEventType;
import jakarta.validation.Valid;

public record SharingRequest(
        TimerEventType eventType,
        @Valid TimerEventInfoRequest data
) {

    public TimerEventInfo toTimerEventInfo() {
        if (data == null) {
            return null;
        }
        return data.toTimerEventInfo();
    }
}
