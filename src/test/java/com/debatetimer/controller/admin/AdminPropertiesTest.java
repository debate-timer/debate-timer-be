package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AdminPropertiesTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 어드민_비밀번호가_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new AdminProperties(empty))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.ADMIN_PASSWORD_EMPTY.getMessage());
        }
    }
}
