package com.debatetimer.controller.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ServerErrorCode {
    ;

    private final HttpStatus status;
    private final String message;

    ServerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
