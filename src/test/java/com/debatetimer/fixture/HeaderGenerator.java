package com.debatetimer.fixture;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.service.auth.JwtTokenProvider;
import com.debatetimer.service.cookie.CookieProvider;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class HeaderGenerator {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieProvider cookieProvider;

    public HeaderGenerator(JwtTokenProvider jwtTokenProvider, CookieProvider cookieProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieProvider = cookieProvider;
    }

    public Headers generateAccessTokenHeader(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(new MemberInfo(member));
        return new Headers(new Header(HttpHeaders.AUTHORIZATION, accessToken));
    }
}
