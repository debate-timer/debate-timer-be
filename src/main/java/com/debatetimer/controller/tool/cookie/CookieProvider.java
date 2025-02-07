package com.debatetimer.controller.tool.cookie;

import java.time.Duration;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    private static final String PATH = "/";
    private static final String SAME_SITE = SameSite.NONE.attributeValue();

    public ResponseCookie createCookie(String key, String value, Duration expiration) {
        return ResponseCookie.from(key, value)
                .maxAge(expiration)
                .path(PATH)
                .sameSite(SAME_SITE)
                .secure(true)
                .build();
    }
}
