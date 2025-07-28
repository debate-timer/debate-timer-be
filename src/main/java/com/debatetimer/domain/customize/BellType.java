package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.function.IntPredicate;

public enum BellType {

    AFTER_START(time -> time >= 0),
    BEFORE_END(time -> time >= 0),
    AFTER_END(time -> time <= 0),
    ;

    private final IntPredicate timeValidator;

    BellType(IntPredicate timeValidator) {
        this.timeValidator = timeValidator;
    }

    public void validateTime(int time) {
        if (!timeValidator.test(time)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_BELL_TIME);
        }
    }
}
