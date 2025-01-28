package com.debatetimer.dto.member;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuthTokenResponse(String access_token) {
}
