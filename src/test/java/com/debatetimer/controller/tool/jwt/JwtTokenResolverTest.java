package com.debatetimer.controller.tool.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.JwtTokenFixture;
import com.debatetimer.fixture.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtTokenResolverTest {

    private TokenGenerator tokenGenerator;
    private JwtTokenResolver jwtTokenResolver;

    @BeforeEach
    void setUp() {
        this.tokenGenerator = new TokenGenerator();
        this.jwtTokenResolver = new JwtTokenResolver(JwtTokenFixture.TEST_TOKEN_PROPERTIES);
    }

    @Nested
    class ResolveAccessToken {

        @Test
        void 액세스_토큰에서_이메일을_가져온다() {
            String email = "bito@gmail.com";
            String accessToken = tokenGenerator.generateAccessToken(email);

            assertThat(jwtTokenResolver.resolveAccessToken(accessToken)).isEqualTo(email);
        }

        @Test
        void 기한이_만료된_토큰이면_예외를_발생시킨다() {
            String expiredToken = tokenGenerator.generateExpiredToken();

            assertThatThrownBy(() -> jwtTokenResolver.resolveAccessToken(expiredToken))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EXPIRED_TOKEN.getMessage());
        }

        @Test
        void 액세스_토큰이_아니면_예외를_발생시킨다() {
            String refreshToken = tokenGenerator.generateRefreshToken("bito@gmail.com");

            assertThatThrownBy(() -> jwtTokenResolver.resolveAccessToken(refreshToken))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.UNAUTHORIZED_MEMBER.getMessage());
        }
    }

    @Nested
    class ResolveRefreshToken {

        @Test
        void 리프레시_토큰에서_이메일을_가져온다() {
            String email = "bito@gmail.com";
            String accessToken = tokenGenerator.generateRefreshToken(email);

            assertThat(jwtTokenResolver.resolveRefreshToken(accessToken)).isEqualTo(email);
        }

        @Test
        void 기한이_만료된_토큰이면_예외를_발생시킨다() {
            String expiredToken = tokenGenerator.generateExpiredToken();

            assertThatThrownBy(() -> jwtTokenResolver.resolveRefreshToken(expiredToken))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EXPIRED_TOKEN.getMessage());
        }

        @Test
        void 리프레시_토큰이_아니면_예외를_발생시킨다() {
            String accessToken = tokenGenerator.generateAccessToken("default@gmail.com");

            assertThatThrownBy(() -> jwtTokenResolver.resolveRefreshToken(accessToken))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.UNAUTHORIZED_MEMBER.getMessage());
        }
    }
}
