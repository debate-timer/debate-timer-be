package com.debatetimer.controller.exception.custom;

import org.springframework.http.HttpStatus;

public class DTServerErrorException extends DTException {

    public DTServerErrorException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
