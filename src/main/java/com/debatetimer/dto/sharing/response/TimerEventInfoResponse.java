package com.debatetimer.dto.sharing.response;


import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.sharing.TimerEventInfo;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TimerEventInfoResponse(
        @NotNull
        CustomizeBoxType timerType,

        @Nullable
        Stance currentTeam,

        int sequence,
        long remainingTime
) {

    public TimerEventInfoResponse(TimerEventInfo timerEventInfo) {
        this(
                timerEventInfo.getTimerType(),
                timerEventInfo.getCurrentTeam(),
                timerEventInfo.getSequence(),
                timerEventInfo.getRemainingTime()
        );
    }
}
