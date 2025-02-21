package com.debatetimer.controller.tool.jwt;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
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
            throw new DTInitializationException(InitializationErrorCode.JWT_SECRET_KEY_EMPTY);
        }
    }

    private void validate(Duration expiration) {
        if (expiration == null) {
            throw new DTInitializationException(InitializationErrorCode.JWT_TOKEN_DURATION_EMPTY);
        }
        if (expiration.isZero() || expiration.isNegative()) {
            throw new DTInitializationException(InitializationErrorCode.JWT_TOKEN_DURATION_INVALID);
        }
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
