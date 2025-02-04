package com.debatetimer.fixture;

import com.debatetimer.controller.tool.cookie.CookieProvider;
import com.debatetimer.controller.tool.jwt.JwtTokenProvider;
import com.debatetimer.dto.member.MemberInfo;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
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
        return generateCookie("refreshToken", refreshToken, Duration.ofHours(1));
    }

    public Cookie[] generateCookie(String cookieName, String value, Duration expiration) {
        ResponseCookie responseCookie = cookieProvider.createCookie(cookieName, value, expiration);

        Cookie servletCookie = new Cookie(responseCookie.getName(), responseCookie.getValue());
        servletCookie.setMaxAge((int) expiration.toSeconds());
        servletCookie.setPath(responseCookie.getPath());
        servletCookie.setSecure(responseCookie.isSecure());
        servletCookie.setHttpOnly(responseCookie.isHttpOnly());
        return new Cookie[]{servletCookie};
    }
}
