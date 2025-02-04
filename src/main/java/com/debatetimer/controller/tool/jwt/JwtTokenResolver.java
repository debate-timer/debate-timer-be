package com.debatetimer.controller.tool.jwt;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenResolver {

    private final JwtTokenProperties jwtTokenProperties;

    public String resolveAccessToken(String accessToken) {
        return resolveToken(accessToken, TokenType.ACCESS_TOKEN);
    }

    public String resolveRefreshToken(String refreshToken) {
        return resolveToken(refreshToken, TokenType.REFRESH_TOKEN);
    }

    private String resolveToken(String token, TokenType tokenType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            validateTokenType(claims, tokenType);
            return claims.getSubject();
        } catch (ExpiredJwtException exception) {
            throw new DTClientErrorException(ClientErrorCode.EXPIRED_TOKEN);
        } catch (JwtException exception) {
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }
    }

    private void validateTokenType(Claims claims, TokenType tokenType) {
        String extractTokenType = claims.get("type", String.class);
        if (!extractTokenType.equals(tokenType.name())) {
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }
    }
}
