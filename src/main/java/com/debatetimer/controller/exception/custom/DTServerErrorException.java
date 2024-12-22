package com.debatetimer.controller.exception.custom;

import com.debatetimer.controller.exception.errorcode.ServerErrorCode;

public class DTServerErrorException extends DTException {

    public DTServerErrorException(ServerErrorCode serverErrorCode) {
        super(serverErrorCode.getMessage(), serverErrorCode.getStatus());
    }
}
