package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BellTypeTest {

    @Nested
    class ValidateTime {

        @Test
        void 벨_타입이_AFTER_START일때_시간은_0이상이어야_한다() {
            assertThatThrownBy(() -> BellType.AFTER_START.validateTime(-1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_TIME.getMessage());
        }

        @Test
        void 벨_타입이_BEFORE_END일때_시간은_0이상이어야_한다() {
            assertThatThrownBy(() -> BellType.BEFORE_END.validateTime(-1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_TIME.getMessage());
        }

        @Test
        void 벨_타입이_AFTER_END일때_시간은_0이하여야_한다() {
            assertThatThrownBy(() -> BellType.AFTER_END.validateTime(1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_TIME.getMessage());
        }
    }
}
