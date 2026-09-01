package com.debatetimer.domain.sharing;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TimerEventData {

    @NotNull
    private final CustomizeBoxType timerType;

    private final int sequence;

    @Nullable
    private final Stance currentTeam;

    private final long remainingTime;

    public TimerEventData(
            CustomizeBoxType timerType,
            int sequence,
            @Nullable Stance currentTeam,
            long remainingTime
    ) {
        validateCurrentTeam(timerType, currentTeam);
        this.timerType = timerType;
        this.sequence = sequence;
        this.currentTeam = currentTeam;
        this.remainingTime = remainingTime;
    }

    private void validateCurrentTeam(CustomizeBoxType timerType, Stance currentTeam) {
        if (timerType.isTimeBased() && currentTeam == null) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BASED_TIMER_EVENT_DATA);
        }

        if (!timerType.isTimeBased() && currentTeam != null) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_NORMAL_TIMER_EVENT_DATA);
        }
    }
}
