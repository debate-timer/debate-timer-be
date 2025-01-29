package com.debatetimer.service.cookie;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    private static final String PATH = "/";

    public Cookie createCookie(String cookieName, String token, long expirationMillis) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setMaxAge((int) (expirationMillis / 1000));
        cookie.setPath(PATH);
        return cookie;
    }

    public Cookie deleteCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath(PATH);
        return cookie;
    }
}
