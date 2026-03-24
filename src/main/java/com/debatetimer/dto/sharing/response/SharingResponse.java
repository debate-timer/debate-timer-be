package com.debatetimer.dto.sharing.response;

import com.debatetimer.domain.sharing.TimerEventType;
import jakarta.annotation.Nullable;

public record SharingResponse(
        TimerEventType eventType,

        @Nullable
        TimerEventInfoResponse data
) {

}
