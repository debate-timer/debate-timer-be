package com.debatetimer.fixture;

import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.service.auth.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenGenerator(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String generateRefreshToken(String nickname, String email) {
        return jwtTokenProvider.createRefreshToken(new MemberInfo(nickname, email));
    }

    public String generateAccessToken(String nickname, String email) {
        return jwtTokenProvider.createAccessToken(new MemberInfo(nickname, email));
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
