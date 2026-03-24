package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.TimerEventInfo;
import com.debatetimer.domain.sharing.TimerEventType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Optional;

public record SharingRequest(
        TimerEventType eventType,

        @Valid
        @Nullable
        TimerEventInfoRequest data
) {

    public Optional<TimerEventInfo> toTimerEventInfo() {
        return Optional.ofNullable(data)
                .map(TimerEventInfoRequest::toTimerEventInfo);
    }
}
