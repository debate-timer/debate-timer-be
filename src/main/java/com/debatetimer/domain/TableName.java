package com.debatetimer.domain;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import lombok.Getter;

@Getter
public class TableName {

    private static final String NAME_REGEX = "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\s]+$";
    public static final int NAME_MAX_LENGTH = 20;

    private final String value;

    public TableName(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String name) {
        if (name.length() > NAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_NAME_LENGTH);
        }
        if (!name.matches(NAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_NAME_FORM);
        }
    }
}
