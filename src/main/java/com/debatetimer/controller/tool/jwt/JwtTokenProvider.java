package com.debatetimer.controller.tool.jwt;

import com.debatetimer.dto.member.MemberInfo;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtTokenProperties jwtTokenProperties;

    public String createAccessToken(MemberInfo memberInfo) {
        Duration accessTokenExpiration = jwtTokenProperties.getAccessTokenExpiration();
        return createToken(memberInfo, accessTokenExpiration, TokenType.ACCESS_TOKEN);
    }

    public String createRefreshToken(MemberInfo memberInfo) {
        Duration refreshTokenExpiration = jwtTokenProperties.getRefreshTokenExpiration();
        return createToken(memberInfo, refreshTokenExpiration, TokenType.REFRESH_TOKEN);
    }

    private String createToken(MemberInfo memberInfo, Duration expiration, TokenType tokenType) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expiration.toMillis());
        return Jwts.builder()
                .setSubject(memberInfo.email())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .claim("type", tokenType.name())
                .signWith(jwtTokenProperties.getSecretKey())
                .compact();
    }
}
