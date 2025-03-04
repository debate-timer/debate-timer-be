package com.debatetimer.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InitializationErrorCode {

    OAUTH_PROPERTIES_EMPTY("OAuth 구성 요소들이 입력되지 않았습니다"),

    CORS_ORIGIN_EMPTY("CORS Origin 은 적어도 한 개 있어야 합니다"),
    CORS_ORIGIN_STRING_BLANK("CORS Origin 에 빈 값이 들어올 수 없습니다"),

    JWT_SECRET_KEY_EMPTY("JWT secretKey 가 입력되지 않았습니다"),
    JWT_TOKEN_DURATION_EMPTY("토큰 만료 기간이 입력되지 않았습니다"),
    JWT_TOKEN_DURATION_INVALID("토큰 만료 기간은 양수이어야 합니다"),
    ;

    private final String message;
}
