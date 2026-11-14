package com.debatetimer.exception.custom;

import com.debatetimer.exception.errorcode.ServerErrorCode;

public class DTServerErrorException extends DTErrorResponseException {

    public DTServerErrorException(ServerErrorCode serverErrorCode) {
        super(serverErrorCode.getMessage(), serverErrorCode.getStatus());
    }

    public DTServerErrorException(ServerErrorCode serverErrorCode, Throwable cause) {
        super(serverErrorCode.getMessage(), serverErrorCode.getStatus(), cause);
    }
}
