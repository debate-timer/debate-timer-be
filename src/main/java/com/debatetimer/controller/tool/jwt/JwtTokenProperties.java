package com.debatetimer.controller.tool.jwt;

import com.debatetimer.exception.custom.DTInitializationException;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtTokenProperties {

    private final String secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtTokenProperties(String secretKey, Duration accessTokenExpiration, Duration refreshTokenExpiration) {
        validate(secretKey);
        validate(accessTokenExpiration);
        validate(refreshTokenExpiration);

        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    private void validate(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new DTInitializationException("JWT secretKey 가 입력되지 않았습니다");
        }
    }

    private void validate(Duration expiration) {
        if (expiration == null) {
            throw new DTInitializationException("토큰 만료 기간이 입력되지 않았습니다");
        }
        if (expiration.isZero() || expiration.isNegative()) {
            throw new DTInitializationException("토큰 만료 기간은 양수이어야 합니다");
        }
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
