package com.debatetimer.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DTErrorResponseException extends RuntimeException {

    private final HttpStatus httpStatus;

    protected DTErrorResponseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
