package com.debatetimer.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ServerErrorCode implements ResponseErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 관리자에게 문의하세요."),
    EXCEL_EXPORT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 변환 과정에서 오류가 발생하였습니다");

    private final HttpStatus status;
    private final String message;

    ServerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
