package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.Getter;

@Getter
public class Bell {

    public static final int MAX_BELL_COUNT = 3;

    private final int time;
    private final int count;

    public Bell(int time, int count) {
        validateTime(time);
        validateCount(count);
        this.time = time;
        this.count = count;
    }

    private void validateTime(int time) {
        if (time < 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_BELL_TIME);
        }
    }

    private void validateCount(int count) {
        if (count <= 0 || count > MAX_BELL_COUNT) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_BELL_COUNT);
        }
    }
}
