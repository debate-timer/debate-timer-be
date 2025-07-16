package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NormalTimeBoxTest {

    @Nested
    class ValidateTime {

        @Test
        void 시간은_0보다_커야_한다() {
            Integer time = 0;

            assertThatThrownBy(() -> new NormalTimeBox(Stance.PROS, "비토", null, time))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 시간은_비어있지_않아야_한다() {
            Integer time = null;

            assertThatThrownBy(() -> new NormalTimeBox(Stance.PROS, "비토", null, time))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 시간은_양수여야_한다() {
            Integer time = 1;

            assertThatCode(() -> new NormalTimeBox(Stance.PROS, "비토", null, time))
                    .doesNotThrowAnyException();
        }
    }
}
