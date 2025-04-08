package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomizeTimeBoxTest {

    @Nested
    class ValidateCustomizeTime {

        @Test
        void 자유토론_타입은_개인_발언_시간과_팀_발언_시간을_입력해야_한다() {
            CustomizeTable table = new CustomizeTable();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론",
                    customizeBoxType, 120, 60, "발언자")
            ).doesNotThrowAnyException();
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
                    () -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", notTimeBasedBoxType, 120, 60,
                            "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 팀_발언_시간은_있으며_개인_발언_시간은_없을_수_있다() {
            CustomizeTable table = new CustomizeTable();
            Integer timePerTeam = 60;
            Integer timePerSpeaking = null;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam, timePerSpeaking, "발언자")).doesNotThrowAnyException();
        }

        @Test
        void 개인_발언_시간은_팀_발언_시간보다_적거나_같아야_한다() {
            CustomizeTable table = new CustomizeTable();
            int timePerTeam = 60;
            int timePerSpeaking = 59;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam, timePerSpeaking, "발언자")).doesNotThrowAnyException();
        }

        @Test
        void 개인_발언_시간이_팀_발언_시간보다_길면_예외가_발생한다() {
            CustomizeTable table = new CustomizeTable();
            int timePerTeam = 60;
            int timePerSpeaking = 61;

            assertThatThrownBy(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam, timePerSpeaking, "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME.getMessage());
        }

        @Test
        void 발언_유형의_길이는_일정_범위_이내여야_한다() {
            CustomizeTable table = new CustomizeTable();
            String longSpeechType = "s".repeat(CustomizeTimeBox.SPEECH_TYPE_MAX_LENGTH + 1);

            assertThatThrownBy(
                    () -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, longSpeechType, CustomizeBoxType.TIME_BASED,
                            120, 60, "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEECH_TYPE_LENGTH.getMessage());
        }
    }

    @Nested
    class getTime {

        @Test
        void 자유_토론_타임_박스의_시간은_팀_당_발언_시간의_배수이어야_한다() {
            int timePerTeam = 300;
            int timePerSpeaking = 120;
            CustomizeTable table = new CustomizeTable();
            CustomizeTimeBox timeBasedTimeBox = new CustomizeTimeBox(table, 1, Stance.CONS, "자유 토론",
                    CustomizeBoxType.TIME_BASED, timePerTeam, timePerSpeaking, "콜리");

            assertThat(timeBasedTimeBox.getTime()).isEqualTo(timePerTeam * CustomizeTimeBox.TIME_MULTIPLIER);
        }
    }
}
