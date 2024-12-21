package com.debatetimer.controller.exception.custom;

import org.springframework.http.HttpStatus;

public class DTUnauthorizedException extends DTException {

    public DTUnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
