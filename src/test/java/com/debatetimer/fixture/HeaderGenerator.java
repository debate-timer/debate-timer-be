package com.debatetimer.fixture;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.service.auth.JwtTokenProvider;
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

    public Headers generateAccessToken(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(new MemberInfo(member.getNickname()));
        return new Headers(new Header(HttpHeaders.AUTHORIZATION, accessToken));
    }
}
