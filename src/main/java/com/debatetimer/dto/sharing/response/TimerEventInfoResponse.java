package com.debatetimer.dto.sharing.response;


import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.sharing.TimerEventInfo;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TimerEventInfoResponse(
        @NotNull CustomizeBoxType timerType,
        int sequence,
        @Nullable Stance currentTeam,
        long remainingTime
) {

    public TimerEventInfoResponse(TimerEventInfo timerEventInfo) {
        this(
                timerEventInfo.getTimerType(),
                timerEventInfo.getSequence(),
                timerEventInfo.getCurrentTeam(),
                timerEventInfo.getRemainingTime()
        );
    }
}
