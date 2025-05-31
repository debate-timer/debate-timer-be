package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomizeTimeBoxTest {

    @Nested
    class ValidateSequence {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 순서는_양수만_가능하다(int sequence) {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatThrownBy(() -> new CustomizeTimeBox(table, sequence, Stance.NEUTRAL, "자유토론",
                    customizeBoxType, 120, 60, "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE.getMessage());
        }
    }

    @Nested
    class ValidateTime {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 시간은_양수만_가능하다(int time) {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.NORMAL;

            assertThatThrownBy(
                    () -> new CustomizeTimeBox(table, 1, Stance.CONS, "자유토론",
                            customizeBoxType, time, 60, "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }
    }

    @Nested
    class ValidateSpeaker {

        @Test
        void 발언자_이름은_일정길이_이내로_허용된다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.NORMAL;
            String speaker = "k".repeat(CustomizeTimeBox.SPEAKER_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new CustomizeTimeBox(table, 1, Stance.CONS, "입론",
                    customizeBoxType, 120, speaker))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEAKER_LENGTH.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 발언자는_빈_값이_허용된다(String speaker) {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.NORMAL;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.CONS, "입론",
                    customizeBoxType, 120, speaker))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"   ", " "})
        @ParameterizedTest
        void 발언자는_공백이_입력되면_null로_저장된다(String speaker) {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.NORMAL;

            CustomizeTimeBox timeBox = new CustomizeTimeBox(table, 1, Stance.CONS, "입론",
                    customizeBoxType, 120, speaker);

            assertThat(timeBox.getSpeaker()).isNull();
        }
    }

    @Nested
    class ValidateCustomizeTime {

        @Test
        void 자유토론_타입은_개인_발언_시간과_팀_발언_시간을_입력해야_한다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론",
                    customizeBoxType, 120, 60, "발언자")
            ).doesNotThrowAnyException();
        }

        @Test
        void 자유토론_타입이_개인_발언_시간과_팀_발언_시간을_입력하지_않을_경우_예외가_발생한다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType customizeBoxType = CustomizeBoxType.TIME_BASED;

            assertThatThrownBy(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", customizeBoxType, 10,
                    "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 자유토론_타입이_아닌_타임박스가_개인_발언_시간과_팀_발언_시간을_입력할_경우_예외가_발생한다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeBoxType notTimeBasedBoxType = CustomizeBoxType.NORMAL;

            assertThatThrownBy(
                    () -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", notTimeBasedBoxType, 120, 60,
                            "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_FORMAT.getMessage());
        }

        @Test
        void 팀_발언_시간은_있으며_개인_발언_시간은_없을_수_있다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            Integer timePerTeam = 60;
            Integer timePerSpeaking = null;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam, timePerSpeaking, "발언자")).doesNotThrowAnyException();
        }

        @Test
        void 개인_발언_시간은_팀_발언_시간보다_적거나_같아야_한다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            int timePerTeam = 60;
            int timePerSpeaking = 59;

            assertThatCode(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam, timePerSpeaking, "발언자")).doesNotThrowAnyException();
        }

        @Test
        void 개인_발언_시간이_팀_발언_시간보다_길면_예외가_발생한다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
            int timePerTeam = 60;
            int timePerSpeaking = 61;

            assertThatThrownBy(() -> new CustomizeTimeBox(table, 1, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED,
                    timePerTeam, timePerSpeaking, "발언자")).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIME.getMessage());
        }

        @Test
        void 발언_유형의_길이는_일정_범위_이내여야_한다() {
            CustomizeTableEntity table = new CustomizeTableEntity();
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
            CustomizeTableEntity table = new CustomizeTableEntity();
            CustomizeTimeBox timeBasedTimeBox = new CustomizeTimeBox(table, 1, Stance.CONS, "자유 토론",
                    CustomizeBoxType.TIME_BASED, timePerTeam, timePerSpeaking, "콜리");

            assertThat(timeBasedTimeBox.getTime()).isEqualTo(timePerTeam * CustomizeTimeBox.TIME_MULTIPLIER);
        }
    }
}
