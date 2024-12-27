package com.debatetimer.controller.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ClientErrorCode implements ErrorCode {

    FIELD_ERROR(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),
    URL_PARAMETER_ERROR(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "입력한 값의 타입이 잘못되었습니다."),
    NO_RESOURCE_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "허용되지 않은 미디어 타입입니다."),
    ALREADY_DISCONNECTED(HttpStatus.BAD_REQUEST, "이미 클라이언트에서 요청이 종료되었습니다."),

    MEMBER_TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "회원님의 토론 테이블을 찾을 수 없습니다."),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다"),
    ;

    private final HttpStatus status;
    private final String message;

    ClientErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
