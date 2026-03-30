package com.debatetimer.domain.sharing;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TimerEventType {

    NEXT(Objects::nonNull),
    BEFORE(Objects::nonNull),
    STOP(Objects::nonNull),
    PLAY(Objects::nonNull),
    RESET(Objects::nonNull),
    TEAM_SWITCH(Objects::nonNull),
    FINISHED(Objects::isNull),
    ;

    private final Predicate<Object> eventDataValidator;

    public void validateEventData(Object eventData) {
        if (!eventDataValidator.test(eventData)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIMER_EVENT);
        }
    }
}
