package com.debatetimer.exception.custom;

import com.debatetimer.exception.errorcode.ServerErrorCode;

public class DTServerErrorException extends DTException {

    public DTServerErrorException(ServerErrorCode serverErrorCode) {
        super(serverErrorCode.getMessage(), serverErrorCode.getStatus());
    }
}
