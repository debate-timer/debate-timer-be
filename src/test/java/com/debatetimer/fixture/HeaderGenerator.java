package com.debatetimer.fixture;

import com.debatetimer.controller.tool.jwt.JwtTokenProvider;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberInfo;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class HeaderGenerator {

    private final JwtTokenProvider jwtTokenProvider;

    public HeaderGenerator(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Headers generateAccessTokenHeader(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(new MemberInfo(member));
        return new Headers(new Header(HttpHeaders.AUTHORIZATION, accessToken));
    }
}
