package com.debatetimer.domain.sharing;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TimerEventInfo {

    @NotNull
    private final CustomizeBoxType timerType;

    private final int sequence;

    @Nullable
    private final Stance currentTeam;

    private final long remainingTime;
}
