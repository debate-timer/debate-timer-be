package com.debatetimer.service.auth;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenResolver {

    private final JwtTokenProperties jwtTokenProperties;

    public String resolveAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            validateTokenType(claims, TokenType.ACCESS_TOKEN);
            return claims.getSubject();
        } catch (ExpiredJwtException exception) {
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
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
