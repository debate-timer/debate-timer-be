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

    /**
     * 이벤트가 반영된 뒤 청중 화면에 표시될 타임박스 순서를 구한다.
     * 이전·다음 이벤트는 이동하기 전 순서를 담고 있어, 이동한 뒤 순서로 바꾼다.
     */
    public int toDisplayedSequence(int sequence) {
        if (this == NEXT) {
            return sequence + 1;
        }
        if (this == BEFORE) {
            return Math.max(sequence - 1, 0);
        }
        return sequence;
    }
}
