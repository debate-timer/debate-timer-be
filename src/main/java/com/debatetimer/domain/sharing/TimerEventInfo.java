package com.debatetimer.domain.sharing;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TimerEventInfo {

    private final CustomizeBoxType timerType;
    private final int sequence;
    private final Stance currentTeam;
    private final long remainingTime;
}
