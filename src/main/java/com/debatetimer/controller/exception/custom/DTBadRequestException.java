package com.debatetimer.controller.exception.custom;

import org.springframework.http.HttpStatus;

public class DTBadRequestException extends DTException {

    public DTBadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
