package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BellTest {

    @Nested
    class Validate {

        @ValueSource(ints = {0, Bell.MAX_BELL_COUNT + 1})
        @ParameterizedTest
        void 벨_횟수는_정해진_횟수_바깥일_경우_생성되지_않는다(int count) {
            assertThatThrownBy(() -> new Bell(BellType.AFTER_START, 1, count))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_COUNT.getMessage());
        }

        @ValueSource(ints = {1, Bell.MAX_BELL_COUNT})
        @ParameterizedTest
        void 벨_횟수는_정해진_횟수_이내여야_한다(int count) {
            assertThatCode(() -> new Bell(BellType.AFTER_START, 1, count))
                    .doesNotThrowAnyException();
        }
    }
}
