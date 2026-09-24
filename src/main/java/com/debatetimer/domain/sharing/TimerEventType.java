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
    SYNC(Objects::nonNull),
    FINISHED(Objects::isNull),
    // 활성 사회자가 없음을 청중에게 알리는 서버 전용 이벤트로, 사회자는 발행할 수 없다
    CHAIRMAN_ABSENT(eventData -> false),
    ;

    private final Predicate<Object> eventDataValidator;

    public void validateEventData(Object eventData) {
        if (!eventDataValidator.test(eventData)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIMER_EVENT);
        }
    }

    public boolean isSync() {
        return this == SYNC;
    }
}
