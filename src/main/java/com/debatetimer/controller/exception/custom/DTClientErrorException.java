package com.debatetimer.controller.exception.custom;

import com.debatetimer.controller.exception.errorcode.ClientErrorCode;

public class DTClientErrorException extends DTException {

    public DTClientErrorException(ClientErrorCode clientErrorCode) {
        super(clientErrorCode.getMessage(), clientErrorCode.getStatus());
    }
}
