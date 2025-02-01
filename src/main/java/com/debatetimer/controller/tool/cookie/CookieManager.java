package com.debatetimer.controller.tool.cookie;

import com.debatetimer.controller.tool.jwt.JwtTokenProperties;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieManager {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final CookieProvider cookieProvider;
    private final CookieExtractor cookieExtractor;
    private final JwtTokenProperties jwtTokenProperties;

    public Cookie createRefreshTokenCookie(String token) {
        return cookieProvider.createCookie(REFRESH_TOKEN_COOKIE_NAME, token,
                jwtTokenProperties.getRefreshTokenExpirationMillis());
    }

    public String extractRefreshToken(Cookie[] cookies) {
        return cookieExtractor.extractCookie(REFRESH_TOKEN_COOKIE_NAME, cookies);
    }

    public Cookie deleteRefreshTokenCookie() {
        return cookieProvider.deleteCookie(REFRESH_TOKEN_COOKIE_NAME);
    }
}
