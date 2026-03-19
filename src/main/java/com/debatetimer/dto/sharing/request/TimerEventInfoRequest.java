package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.sharing.TimerEventInfo;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TimerEventInfoRequest(
        @NotNull CustomizeBoxType timerType,
        int sequence,
        @Nullable Stance currentTeam,
        long time
) {

    public TimerEventInfo toTimerEventInfo() {
        return new TimerEventInfo(
                timerType,
                sequence,
                currentTeam,
                time
        );
    }
}
