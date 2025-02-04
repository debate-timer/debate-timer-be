package com.debatetimer.controller.tool.cookie;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    private static final String PATH = "/";

    public ResponseCookie createCookie(String cookieName, String token, long expirationMillis) {
        return ResponseCookie.from(cookieName, token)
                .maxAge(Duration.ofMillis(expirationMillis))
                .path(PATH)
                .sameSite("None")
                .secure(true)
                .build();
    }

    public ResponseCookie deleteCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .maxAge(0)
                .path(PATH)
                .sameSite("None")
                .secure(true)
                .build();
    }
}
