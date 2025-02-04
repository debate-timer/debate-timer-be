package com.debatetimer.fixture;

import com.debatetimer.controller.tool.cookie.CookieProvider;
import com.debatetimer.controller.tool.jwt.JwtTokenProvider;
import com.debatetimer.dto.member.MemberInfo;
import jakarta.servlet.http.Cookie;
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
        return generateCookie("refreshToken", refreshToken, 100000);
    }

    public Cookie[] generateCookie(String cookieName, String value, long expirationMills) {
        ResponseCookie responseCookie = cookieProvider.createCookie(cookieName, value, expirationMills);

        Cookie servletCookie = new Cookie(responseCookie.getName(), responseCookie.getValue());
        servletCookie.setMaxAge((int) (expirationMills / 1000));
        servletCookie.setPath(responseCookie.getPath());
        servletCookie.setSecure(responseCookie.isSecure());
        servletCookie.setHttpOnly(responseCookie.isHttpOnly());
        return new Cookie[]{servletCookie};
    }
}
