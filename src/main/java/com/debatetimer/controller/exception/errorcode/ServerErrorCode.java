package com.debatetimer.controller.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ServerErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 관리자에게 문의하세요."),
    ;

    private final HttpStatus status;
    private final String message;

    ServerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
