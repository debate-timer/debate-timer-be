package com.debatetimer.domain.poll;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.Getter;

@Getter
public class ParticipateCode {

    private final String value;

    public ParticipateCode(String value) {
        validateName(value);
        this.value = value;
    }

    private void validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_POLL_PARTICIPANT_CODE);
        }
    }
}
