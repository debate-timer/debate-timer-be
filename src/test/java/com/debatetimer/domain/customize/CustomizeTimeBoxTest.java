package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomizeTimeBoxTest {

    @Nested
    class ValidateCustomize {

        @Test
        void 자유토론_타입은_총_시간이_팀_발언_시간의_2배여야_한다() {
            CustomizeTable table = new CustomizeTable();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatThrownBy(
                    () -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", customizeBoxType, 150, 120, 60,
                            "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME_IS_NOT_DOUBLE.getMessage());
        }

        @Test
        void 자유토론_타입은_개인_발언_시간과_팀_발언_시간을_입력해야_한다() {
            CustomizeTable table = new CustomizeTable();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", customizeBoxType, 240, 120, 60,
                    "발언자")).doesNotThrowAnyException();
        }

        @Test
        void 자유토론_타입이_개인_발언_시간과_팀_발언_시간을_입력하지_않을_경우_예외가_발생한다() {
            CustomizeTable table = new CustomizeTable();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatThrownBy(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", customizeBoxType, 10,
                    "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 자유토론_타입이_아닌_타임박스가_개인_발언_시간과_팀_발언_시간을_입력할_경우_예외가_발생한다() {
            CustomizeTable table = new CustomizeTable();
            CustomizeBoxType notTimeBasedBoxType = CustomizeBoxType.NORMAL;

            assertThatThrownBy(
                    () -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", notTimeBasedBoxType, 240, 120, 60,
                            "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 개인_발언_시간은_팀_발언_시간보다_적거나_같아야_한다() {
            CustomizeTable table = new CustomizeTable();
            int timePerTeam = 60;
            int timePerSpeaking = 59;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam * 2, timePerTeam, timePerSpeaking, "발언자")).doesNotThrowAnyException();
        }

        @Test
        void 개인_발언_시간이_팀_발언_시간보다_길면_예외가_발생한다() {
            CustomizeTable table = new CustomizeTable();
            int timePerTeam = 60;
            int timePerSpeaking = 61;

            assertThatThrownBy(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam * 2, timePerTeam, timePerSpeaking, "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME.getMessage());
        }
    }
}
