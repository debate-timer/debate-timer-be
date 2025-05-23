package com.debatetimer.controller.tool.jwt;

import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.dto.member.MemberInfo;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthManager {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenResolver jwtTokenResolver;

    public JwtTokenResponse issueToken(MemberInfo memberInfo) {
        String accessToken = jwtTokenProvider.createAccessToken(memberInfo);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberInfo);
        Duration refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpiration();
        return new JwtTokenResponse(accessToken, refreshToken, refreshTokenExpiration);
    }

    public JwtTokenResponse reissueToken(String refreshToken) {
        String email = jwtTokenResolver.resolveRefreshToken(refreshToken);
        MemberInfo memberInfo = new MemberInfo(email);

        String accessToken = jwtTokenProvider.createAccessToken(memberInfo);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberInfo);
        Duration refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpiration();
        return new JwtTokenResponse(accessToken, newRefreshToken, refreshTokenExpiration);
    }

    public String resolveAccessToken(String accessToken) {
        return jwtTokenResolver.resolveAccessToken(accessToken);
    }

    public String resolveRefreshToken(String refreshToken) {
        return jwtTokenResolver.resolveRefreshToken(refreshToken);
    }
}
