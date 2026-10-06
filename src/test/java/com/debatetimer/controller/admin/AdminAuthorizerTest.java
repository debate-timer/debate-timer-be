package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AdminAuthorizerTest {

    private final AdminAuthorizer adminAuthorizer = new AdminAuthorizer(new AdminProperties("admin-password"));

    @Nested
    class Authorize {

        @Test
        void 비밀번호가_일치하면_통과한다() {
            assertThatCode(() -> adminAuthorizer.authorize("admin-password"))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"wrong-password", "admin-password ", "admin"})
        void 비밀번호가_일치하지_않으면_예외를_던진다(String password) {
            assertThatThrownBy(() -> adminAuthorizer.authorize(password))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_ADMIN_PASSWORD.getMessage());
        }
    }
}
