package com.debatetimer.controller.tool.cookie;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieManager {

    private static final String EMPTY_TOKEN = "";
    private static final Duration EXPIRED_DURATION = Duration.ZERO;

    private final CookieProvider cookieProvider;

    public ResponseCookie createCookie(String key, String value, Duration expiration) {
        return cookieProvider.createCookie(key, value, expiration);
    }

    public ResponseCookie createExpiredCookie(String key) {
        return cookieProvider.createCookie(key, EMPTY_TOKEN, EXPIRED_DURATION);
    }
}
