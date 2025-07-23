package com.debatetimer.domain.poll;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.Getter;

@Getter
public class ParticipantName {

    private final String value;

    public ParticipantName(String value) {
        validateName(value);
        this.value = value;
    }

    private void validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_POLL_PARTICIPANT_NAME);
        }
    }
}
