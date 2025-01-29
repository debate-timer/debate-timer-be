package com.debatetimer.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JwtTokenResolverTest extends BaseServiceTest {

    @Autowired
    private JwtTokenResolver jwtTokenResolver;

    @Nested
    class ResolveAccessToken {

        @Test
        void 액세스_토큰에서_닉네임을_가져온다() {
            String nickname = "비토";
            String accessToken = tokenGenerator.generateAccessToken(nickname);

            assertThat(jwtTokenResolver.resolveAccessToken(accessToken)).isEqualTo(nickname);
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
            String refreshToken = tokenGenerator.generateRefreshToken("바토");

            assertThatThrownBy(() -> jwtTokenResolver.resolveAccessToken(refreshToken))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.UNAUTHORIZED_MEMBER.getMessage());
        }
    }

    @Nested
    class ResolveRefreshToken {

        @Test
        void 리프레시_토큰에서_닉네임을_가져온다() {
            String nickname = "비토";
            String accessToken = tokenGenerator.generateRefreshToken(nickname);

            assertThat(jwtTokenResolver.resolveRefreshToken(accessToken)).isEqualTo(nickname);
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
            String accessToken = tokenGenerator.generateAccessToken("바토");

            assertThatThrownBy(() -> jwtTokenResolver.resolveRefreshToken(accessToken))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.UNAUTHORIZED_MEMBER.getMessage());
        }
    }
}