package com.debatetimer.entity.customize;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BellTest {

    @Nested
    class Validate {

        @Test
        void 벨_시간은_0이상이어야_한다() {
            assertThatThrownBy(() -> new Bell(null, -1, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_TIME.getMessage());
        }

        @Test
        void 벨_횟수는_1이상이어야_한다() {
            assertThatThrownBy(() -> new Bell(null, 1, 0))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_COUNT.getMessage());
        }
    }
}
