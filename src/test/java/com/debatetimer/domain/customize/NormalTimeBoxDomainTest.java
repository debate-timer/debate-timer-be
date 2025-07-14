package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NormalTimeBoxDomainTest {

    @Nested
    class ValidateTime {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        void 시간은_0보다_커야_한다(int time) {
            assertThatThrownBy(() -> new NormalTimeBoxDomain(Stance.PROS, "비토", null, time))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 시간은_비어있지_않아야_한다() {
            Integer time = null;

            assertThatThrownBy(() -> new NormalTimeBoxDomain(Stance.PROS, "비토", null, time))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 시간은_양수여야_한다() {
            int time = 1;

            assertThatCode(() -> new NormalTimeBoxDomain(Stance.PROS, "비토", null, time))
                    .doesNotThrowAnyException();
        }
    }
}
