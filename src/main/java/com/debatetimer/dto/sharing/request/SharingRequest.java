package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

public record SharingRequest(
        @NotNull
        TimerEventType eventType,

        @Nullable
        Long version,

        @Valid
        @Nullable
        TimerEventInfoRequest data
) {

    public TimerEvent toTimerEvent() {
        if (eventType.requiresVersion() && version == null) {
            throw new DTClientErrorException(ClientErrorCode.VERSION_REQUIRED_TIMER_EVENT);
        }
        return Optional.ofNullable(data)
                .map(TimerEventInfoRequest::toTimerEventInfo)
                .map(eventData -> new TimerEvent(eventType, eventData))
                .orElseGet(() -> new TimerEvent(eventType));
    }
}
