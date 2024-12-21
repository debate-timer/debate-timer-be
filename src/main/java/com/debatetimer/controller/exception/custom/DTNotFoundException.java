package com.debatetimer.controller.exception.custom;

import org.springframework.http.HttpStatus;

public class DTNotFoundException extends DTException {

    public DTNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
