package com.debatetimer.exception.custom;

import com.debatetimer.exception.errorcode.ClientErrorCode;

public class DTClientErrorException extends DTErrorResponseException {

    public DTClientErrorException(ClientErrorCode clientErrorCode) {
        super(clientErrorCode.getMessage(), clientErrorCode.getStatus());
    }
}
