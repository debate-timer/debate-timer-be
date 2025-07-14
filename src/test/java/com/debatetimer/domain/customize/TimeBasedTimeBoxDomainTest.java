package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeBasedTimeBoxDomainTest {

    @Nested
    class ValidateStance {

        @Test
        void 중립_스탠스가_아니면_예외가_발생한다() {
            Stance stance = Stance.PROS;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBoxDomain(stance, "자유발언", "비토", 120, 60))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_STANCE.getMessage());
        }

        @Test
        void 중립_스탠스면_예외가_발생하지_않는다() {
            Stance stance = Stance.NEUTRAL;

            assertThatCode(
                    () -> new TimeBasedTimeBoxDomain(stance, "자유발언", "비토", 120, 60))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class ValidateTimes {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void 팀_당_발언_시간이_양수이어야_한다(int timePerTeam) {
            int timePerSpeaking = 1;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBoxDomain(Stance.NEUTRAL, "자유발언", "비토", timePerTeam, timePerSpeaking))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 팀_당_발언_시간이_비어있으면_안된다() {
            Integer timePerTeam = null;
            int timePerSpeaking = 1;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBoxDomain(Stance.NEUTRAL, "자유발언", "비토", timePerTeam, timePerSpeaking))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void 개인_당_시간이_양수이어야_한다(int timePerSpeaking) {
            int timePerTeam = 1;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBoxDomain(Stance.NEUTRAL, "자유발언", "비토", timePerTeam, timePerSpeaking))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }

        @Test
        void 개인_당_발언_시간은_비어있을_수_있다() {
            Integer timePerSpeaking = null;
            int timePerTeam = 1;

            assertThatCode(
                    () -> new TimeBasedTimeBoxDomain(Stance.NEUTRAL, "자유발언", "비토", timePerTeam, timePerSpeaking))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 60, 120})
        void 팀_당_발언시간은_개인_발언시간보다_많거나_같아야_한다(int timePerTeam) {
            int timePerSpeaking = timePerTeam + 1;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBoxDomain(Stance.NEUTRAL, "자유발언", "비토", timePerTeam, timePerSpeaking))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME.getMessage());
        }
    }
}
