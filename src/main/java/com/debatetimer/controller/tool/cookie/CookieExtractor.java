package com.debatetimer.controller.tool.cookie;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class CookieExtractor {

    public String extractCookie(String cookieName, Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.EMPTY_COOKIE));
    }
}
