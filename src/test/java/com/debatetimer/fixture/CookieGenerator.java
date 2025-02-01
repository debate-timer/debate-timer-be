package com.debatetimer.fixture;

import com.debatetimer.controller.tool.cookie.CookieProvider;
import com.debatetimer.controller.tool.jwt.JwtTokenProvider;
import com.debatetimer.dto.member.MemberInfo;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieGenerator {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieProvider cookieProvider;

    public CookieGenerator() {
        this.jwtTokenProvider = new JwtTokenProvider(JwtTokenFixture.TEST_TOKEN_PROPERTIES);
        this.cookieProvider = new CookieProvider();
    }

    public Cookie[] generateRefreshCookie(String email) {
        String refreshToken = jwtTokenProvider.createRefreshToken(new MemberInfo(email));
        return generateCookie("refreshToken", refreshToken, 100000);
    }

    public Cookie[] generateCookie(String cookieName, String value, long expirationMills) {
        Cookie[] cookies = new Cookie[1];
        cookies[0] = cookieProvider.createCookie(cookieName, value, expirationMills);
        return cookies;
    }
}
