package com.debatetimer.dto.sharing.response;


import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.sharing.TimerEventData;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TimerEventDataResponse(
        @NotNull
        CustomizeBoxType timerType,

        @Nullable
        Stance currentTeam,

        int sequence,
        long remainingTime,

        @Nullable
        Boolean isRunning,

        @Nullable
        Long prosRemainingTime,

        @Nullable
        Long consRemainingTime
) {

    public TimerEventDataResponse(TimerEventData timerEventInfo) {
        this(
                timerEventInfo.getTimerType(),
                timerEventInfo.getCurrentTeam(),
                timerEventInfo.getSequence(),
                timerEventInfo.getRemainingTime(),
                timerEventInfo.getIsRunning(),
                timerEventInfo.getProsRemainingTime(),
                timerEventInfo.getConsRemainingTime()
        );
    }
}
