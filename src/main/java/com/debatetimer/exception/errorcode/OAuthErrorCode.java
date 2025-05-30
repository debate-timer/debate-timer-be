package com.debatetimer.exception.errorcode;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuthErrorCode {

    INVALID_REDIRECT_URI("redirect_uri_mismatch", HttpStatus.BAD_REQUEST, "구글 OAuth redirect_uri가 잘못되었습니다"),
    INVALID_AUTHORIZATION_CODE("invalid_grant", HttpStatus.BAD_REQUEST, "구글 OAuth 인증 코드가 잘못되었습니다."),
    ;

    private final String error;
    private final HttpStatus status;
    private final String message;

    public static Optional<OAuthErrorCode> mapTo(String error) {
        return Stream.of(values())
                .filter(errorCode -> error.equals(errorCode.getError()))
                .findAny();
    }
}
