package com.debatetimer.service.cookie;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieExtractor {

    public String extractCookie(String cookieName, Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow();
    }
}
