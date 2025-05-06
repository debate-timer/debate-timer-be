package com.debatetimer.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthErrorResponse(
        @JsonProperty("ErrorCode")
        String errorCode,

        @JsonProperty("Error")
        String error
) {

}
