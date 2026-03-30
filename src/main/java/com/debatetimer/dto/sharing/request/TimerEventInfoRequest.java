package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.sharing.TimerEventData;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TimerEventInfoRequest(
        @NotNull
        CustomizeBoxType timerType,

        @Nullable
        Stance currentTeam,

        int sequence,
        long remainingTime
) {

    public TimerEventData toTimerEventInfo() {
        return new TimerEventData(
                timerType,
                sequence,
                currentTeam,
                remainingTime
        );
    }
}
