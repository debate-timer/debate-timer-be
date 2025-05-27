package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.Getter;

@Getter
public class TeamName {

    private static final String NAME_REGEX = "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\s]+$";
    public static final int NAME_MAX_LENGTH = 8;

    private final String value;

    public TeamName(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String teamName) {
        if (teamName.isBlank() || teamName.length() > NAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TEAM_NAME_LENGTH);
        }
        if (!teamName.matches(NAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TEAM_NAME_FORM);
        }
    }
}
