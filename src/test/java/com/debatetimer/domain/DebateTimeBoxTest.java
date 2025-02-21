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

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 순서는_양수만_가능하다(int sequence) {
            assertThatThrownBy(() -> new DebateTimeBoxTestObject(sequence, Stance.CONS, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE.getMessage());
        }

        @Test
        void 발표자_번호는_빈_값이_허용된다() {
            Integer speaker = null;

            assertThatCode(() -> new DebateTimeBoxTestObject(1, Stance.CONS, speaker))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 발표자_번호는_양수만_가능하다(int speaker) {
            assertThatThrownBy(() -> new DebateTimeBoxTestObject(1, Stance.CONS, speaker))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEAKER.getMessage());
        }
    }

    private static class DebateTimeBoxTestObject extends DebateTimeBox {

        public DebateTimeBoxTestObject(int sequence, Stance stance, Integer speaker) {
            super(sequence, stance, speaker);
        }
    }
}
