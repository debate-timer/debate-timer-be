package com.debatetimer.fixture;

import com.debatetimer.controller.tool.jwt.JwtTokenProvider;
import com.debatetimer.dto.member.MemberInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenGenerator() {
        this.jwtTokenProvider = new JwtTokenProvider(JwtTokenFixture.TEST_TOKEN_PROPERTIES);
    }

    public String generateRefreshToken(String email) {
        return jwtTokenProvider.createRefreshToken(new MemberInfo(email));
    }

    public String generateAccessToken(String email) {
        return jwtTokenProvider.createAccessToken(new MemberInfo(email));
    }

    public String generateExpiredToken() {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000);
        SecretKey secretKey = Keys.hmacShaKeyFor("test".repeat(8).getBytes());
        return Jwts.builder()
                .setSubject("")
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .claim("type", "")
                .signWith(secretKey)
                .compact();
    }
}
