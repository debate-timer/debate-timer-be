package com.debatetimer.service.auth;

import com.debatetimer.dto.member.MemberInfo;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtTokenProperties jwtTokenProperties;

    public String createAccessToken(MemberInfo memberInfo) {
        long accessTokenExpirationMillis = jwtTokenProperties.getAccessTokenExpirationMillis();
        return createToken(memberInfo, accessTokenExpirationMillis, TokenType.ACCESS_TOKEN);
    }

    public String createRefreshToken(MemberInfo memberInfo) {
        long refreshTokenExpirationMillis = jwtTokenProperties.getRefreshTokenExpirationMillis();
        return createToken(memberInfo, refreshTokenExpirationMillis, TokenType.REFRESH_TOKEN);
    }

    private String createToken(MemberInfo memberInfo, long expirationMillis, TokenType tokenType) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(memberInfo.nickname())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .claim("type", tokenType.name())
                .signWith(jwtTokenProperties.getSecretKey())
                .compact();
    }
}
