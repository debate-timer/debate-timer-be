package com.debatetimer.fixture;

import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.service.auth.JwtTokenProvider;
import com.debatetimer.service.cookie.CookieProvider;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieGenerator {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieProvider cookieProvider;

    public CookieGenerator(JwtTokenProvider jwtTokenProvider, CookieProvider cookieProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieProvider = cookieProvider;
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
