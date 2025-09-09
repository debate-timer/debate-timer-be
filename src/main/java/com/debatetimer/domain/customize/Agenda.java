package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.Getter;

@Getter
public class Agenda {

    public static final int AGENDA_MAX_LENGTH = 255;

    private final String value;

    public Agenda(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null) {
            return;
        }

        if (value.length() > AGENDA_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_AGENDA_LENGTH);
        }
    }
}
