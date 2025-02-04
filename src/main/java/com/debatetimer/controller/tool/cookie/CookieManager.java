package com.debatetimer.controller.tool.cookie;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieManager {

    private final CookieProvider cookieProvider;

    public ResponseCookie createCookie(String key, String value, Duration expiration) {
        return cookieProvider.createCookie(key, value, expiration);
    }

    public ResponseCookie deleteCookie(String key) {
        return cookieProvider.deleteCookie(key);
    }
}
