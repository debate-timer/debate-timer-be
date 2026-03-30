package com.debatetimer.domain.sharing;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TimerEvent {

    @NotNull
    private final TimerEventType eventType;

    @Nullable
    private final TimerEventData timerEventData;

    public TimerEvent(
            TimerEventType eventType,
            @Nullable TimerEventData timerEventData
    ) {
        eventType.validateEventData(timerEventData);
        this.eventType = eventType;
        this.timerEventData = timerEventData;
    }
}
