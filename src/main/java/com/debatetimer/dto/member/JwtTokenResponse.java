package com.debatetimer.dto.member;

import java.time.Duration;

public record JwtTokenResponse(String accessToken, String refreshToken, Duration refreshExpiration) {

}
