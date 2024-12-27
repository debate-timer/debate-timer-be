package com.debatetimer.controller.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DTException extends RuntimeException {

    private final HttpStatus httpStatus;

    public DTException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
