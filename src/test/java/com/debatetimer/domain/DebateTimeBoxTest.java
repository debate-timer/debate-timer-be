package com.debatetimer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DebateTimeBoxTest {

    @Nested
    class ValidateSequence {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 순서는_양수만_가능하다(int sequence) {
            assertThatThrownBy(() -> new DebateTimeBoxTestObject(sequence, Stance.CONS, 60, "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE.getMessage());
        }


    }

    @Nested
    class ValidateTime {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 시간은_양수만_가능하다(int time) {
            assertThatThrownBy(
                    () -> new DebateTimeBoxTestObject(1, Stance.CONS, time, "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }
    }

    @Nested
    class ValidateSpeaker {

        @Test
        void 발언자_이름은_일정길이_이내로_허용된다() {
            String speaker = "k".repeat(DebateTimeBox.SPEAKER_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new DebateTimeBoxTestObject(1, Stance.CONS, 60, speaker))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEAKER_LENGTH.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 발언자는_빈_값이_허용된다(String speaker) {
            assertThatCode(() -> new DebateTimeBoxTestObject(1, Stance.CONS, 60, speaker))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"   ", " "})
        @ParameterizedTest
        void 발언자는_공백이_입력되면_null로_저장된다(String speaker) {
            DebateTimeBoxTestObject timeBox = new DebateTimeBoxTestObject(1, Stance.CONS, 60, speaker);

            assertThat(timeBox.getSpeaker()).isNull();
        }
    }

    private static class DebateTimeBoxTestObject extends DebateTimeBox {

        public DebateTimeBoxTestObject(int sequence, Stance stance, int time, String speaker) {
            super(sequence, stance, time, speaker);
        }
    }
}
