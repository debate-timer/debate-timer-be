package com.debatetimer.exception.custom;

import com.debatetimer.exception.errorcode.OAuthErrorCode;
import lombok.Getter;

@Getter
public class DTOAuthClientException extends DTErrorResponseException {

    private final String errorResponse;

    public DTOAuthClientException(OAuthErrorCode code, String errorResponse) {
        super(code.getMessage(), code.getStatus());
        this.errorResponse = errorResponse;
    }
}
