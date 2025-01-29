package com.debatetimer.service.cookie;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    public Cookie createCookie(String tokenName, String token, long expirationMillis) {
        Cookie cookie = new Cookie(tokenName, token);
        cookie.setMaxAge((int) (expirationMillis / 1000));
        cookie.setPath("/");
        return cookie;
    }
}
