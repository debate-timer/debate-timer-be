package com.debatetimer.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DebateTimeBoxTest {

    @Nested
    class Validate {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 순서는_양수만_가능하다(int sequence) {
            assertThatThrownBy(() -> new DebateTimeBoxTestObject(sequence, Stance.CONS, 60, "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE.getMessage());
        }

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 시간은_양수만_가능하다(int time) {
            assertThatThrownBy(
                    () -> new DebateTimeBoxTestObject(1, Stance.CONS, time, "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 발표자_번호는_빈_값이_허용된다() {
            String speaker = null;

            assertThatCode(() -> new DebateTimeBoxTestObject(1, Stance.CONS, 60, speaker))
                    .doesNotThrowAnyException();
        }
    }

    private static class DebateTimeBoxTestObject extends DebateTimeBox {

        public DebateTimeBoxTestObject(int sequence, Stance stance, int time, String speaker) {
            super(sequence, stance, time, speaker);
        }
    }
}
