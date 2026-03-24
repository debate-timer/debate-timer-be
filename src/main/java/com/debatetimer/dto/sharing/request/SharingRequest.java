package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.TimerEventInfo;
import com.debatetimer.domain.sharing.TimerEventType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

public record SharingRequest(
        @NotNull
        TimerEventType eventType,

        @Valid
        @Nullable
        TimerEventInfoRequest data
) {

    public boolean hasEventData() {
        return data != null;
    }

    public TimerEventInfo toTimerEventInfo() {
        return Optional.ofNullable(data)
                .map(TimerEventInfoRequest::toTimerEventInfo)
                .orElse(null);
    }
}
