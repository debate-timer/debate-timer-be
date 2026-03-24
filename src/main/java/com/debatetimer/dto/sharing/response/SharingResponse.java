package com.debatetimer.dto.sharing.response;

import com.debatetimer.domain.sharing.TimerEventInfo;
import com.debatetimer.domain.sharing.TimerEventType;
import jakarta.annotation.Nullable;
import java.util.Optional;

public record SharingResponse(
        TimerEventType eventType,

        @Nullable
        TimerEventInfoResponse data
) {

    public SharingResponse(
            TimerEventType eventType,
            Optional<TimerEventInfo> timerEventInfo
    ) {
        this(
                eventType,
                timerEventInfo
                        .map(TimerEventInfoResponse::new)
                        .orElse(null)
        );
    }
}
