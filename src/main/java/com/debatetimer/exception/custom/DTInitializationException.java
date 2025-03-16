package com.debatetimer.exception.custom;

import com.debatetimer.exception.errorcode.InitializationErrorCode;

public class DTInitializationException extends RuntimeException {

    public DTInitializationException(InitializationErrorCode errorCode) {
        super(errorCode.getMessage());
    }
}
