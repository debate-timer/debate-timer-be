package com.debatetimer.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class CorsConfigTest {

    @Nested
    class Validate {

        @Test
        void 허용된_도메인이_null_일_경우_예외를_발생시칸다() {
            assertThatThrownBy(() -> new CorsConfig(null))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.CORS_ORIGIN_EMPTY.getMessage());
        }

        @Test
        void 허용된_도메인이_빈_배열일_경우_예외를_발생시칸다() {
            assertThatThrownBy(() -> new CorsConfig(new String[0]))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.CORS_ORIGIN_EMPTY.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void 허용된_도메인_중에_빈_값이_있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new CorsConfig(new String[]{empty}))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.CORS_ORIGIN_STRING_BLANK.getMessage());

        }
    }
}
