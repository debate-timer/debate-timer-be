package com.debatetimer.domain.timebased;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeBasedTimeBoxTest {

    @Nested
    class ValidateStance {

        @Test
        void 박스타입에_가능한_입장을_검증한다() {
            TimeBasedTable table = new TimeBasedTable();

            assertThatCode(() -> new TimeBasedTimeBox(table, 1, Stance.CONS, TimeBasedBoxType.OPENING, 10, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        void 박스타입에_불가한_입장으로_생성을_시도하면_예외를_발생시킨다() {
            TimeBasedTable table = new TimeBasedTable();

            assertThatThrownBy(
                    () -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, TimeBasedBoxType.OPENING, 10, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_STANCE.getMessage());
        }
    }

    @Nested
    class ValidateTimeBased {

        @Test
        void 시간총량제_타입은_총_시간이_팀_발언_시간의_2배여야_한다() {
            TimeBasedTable table = new TimeBasedTable();
            TimeBasedBoxType timeBasedBoxType = TimeBasedBoxType.TIME_BASED;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, timeBasedBoxType, 150, 120, 60, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME_IS_NOT_DOUBLE.getMessage());
        }

        @Test
        void 시간총량제_타입은_개인_발언_시간과_팀_발언_시간을_입력해야_한다() {
            TimeBasedTable table = new TimeBasedTable();
            TimeBasedBoxType timeBasedBoxType = TimeBasedBoxType.TIME_BASED;

            assertThatCode(() -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, timeBasedBoxType, 240, 120, 60, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        void 시간총량제_타입이_개인_발언_시간과_팀_발언_시간을_입력하지_않을_경우_예외가_발생한다() {
            TimeBasedTable table = new TimeBasedTable();
            TimeBasedBoxType timeBasedBoxType = TimeBasedBoxType.TIME_BASED;

            assertThatThrownBy(() -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, timeBasedBoxType, 10, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 시간총량제가_아닌_타입이__개인_발언_시간과_팀_발언_시간을_입력할_경우_예외가_발생한다() {
            TimeBasedTable table = new TimeBasedTable();
            TimeBasedBoxType notTimeBasedBoxType = TimeBasedBoxType.TIME_OUT;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, notTimeBasedBoxType, 240, 120, 60, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 개인_발언_시간은_팀_발언_시간보다_적거나_같아야_한다() {
            TimeBasedTable table = new TimeBasedTable();
            int timePerTeam = 60;
            int timePerSpeaking = 59;

            assertThatCode(
                    () -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, timePerTeam * 2,
                            timePerTeam, timePerSpeaking, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        void 개인_발언_시간이_팀_발언_시간보다_길면_예외가_발생한다() {
            TimeBasedTable table = new TimeBasedTable();
            int timePerTeam = 60;
            int timePerSpeaking = 61;

            assertThatThrownBy(
                    () -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, timePerTeam * 2,
                            timePerTeam, timePerSpeaking, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME.getMessage());
        }
    }

    @Nested
    class ValidateSpeakerNumber {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 시간총량제_타임박스의_발표자_번호는_양수만_가능하다(int speaker) {
            TimeBasedTable table = new TimeBasedTable();

            assertThatThrownBy(() -> new TimeBasedTimeBox(table, 1, Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED,
                    240, 120, 60, speaker))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEAKER.getMessage());
        }
    }
}
