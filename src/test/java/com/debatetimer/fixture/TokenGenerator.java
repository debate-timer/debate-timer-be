package com.debatetimer.fixture;

import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.service.auth.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenGenerator {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenGenerator(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String generateRefreshToken(String nickname) {
        return jwtTokenProvider.createRefreshToken(new MemberInfo(nickname));
    }

    public String generateAccessToken(String nickname) {
        return jwtTokenProvider.createAccessToken(new MemberInfo(nickname));
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
