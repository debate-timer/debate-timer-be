package com.debatetimer.controller.tool.cookie;

import java.time.Duration;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    private static final String EMPTY_TOKEN = "";
    private static final String PATH = "/";
    private static final String SAME_SITE = SameSite.NONE.attributeValue();

    public ResponseCookie createCookie(String cookieName, String token, Duration expiration) {
        return ResponseCookie.from(cookieName, token)
                .maxAge(expiration)
                .path(PATH)
                .sameSite(SAME_SITE)
                .secure(true)
                .build();
    }

    public ResponseCookie deleteCookie(String cookieName) {
        return ResponseCookie.from(cookieName, EMPTY_TOKEN)
                .maxAge(Duration.ZERO)
                .path(PATH)
                .sameSite(SAME_SITE)
                .secure(true)
                .build();
    }
}
