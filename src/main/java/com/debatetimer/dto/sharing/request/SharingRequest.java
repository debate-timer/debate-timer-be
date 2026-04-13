package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.TimerEvent;
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

    public TimerEvent toTimerEvent() {
        return Optional.ofNullable(data)
                .map(TimerEventInfoRequest::toTimerEventInfo)
                .map(eventData -> new TimerEvent(eventType, eventData))
                .orElseGet(() -> new TimerEvent(eventType));
    }
}
