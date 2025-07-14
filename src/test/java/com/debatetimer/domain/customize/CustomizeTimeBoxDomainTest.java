package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomizeTimeBoxDomainTest {

    @Nested
    class ValidateStance {

        @Test
        void 발언_입장은_비어있을_수_없다() {
            assertThatThrownBy(() -> new InheritedCustomizeTimeBoxDomain(null, "비토", "발언자"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_STANCE.getMessage());
        }

        @Test
        void 발언_입장은_유효한_값이어야_한다() {
            assertThatCode(() -> new InheritedCustomizeTimeBoxDomain(Stance.PROS, "비토", "발언자"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class ValidateSpeechType {

        @Test
        void 발언_종류는_특정_글자를_초과할_수_없다() {
            String speechType = "a".repeat(CustomizeTimeBoxDomain.SPEECH_TYPE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new InheritedCustomizeTimeBoxDomain(Stance.PROS, speechType, "비토"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEECH_TYPE_LENGTH.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\n\t"})
        void 발언_종류는_비어있을_수_없다(String emptySpeechType) {
            assertThatThrownBy(() -> new InheritedCustomizeTimeBoxDomain(Stance.PROS, emptySpeechType, "비토"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEECH_TYPE_LENGTH.getMessage());
        }

        @Test
        void 발언_종류는_특정_글자_이내이어야_한다() {
            String speechType = "a".repeat(CustomizeTimeBoxDomain.SPEECH_TYPE_MAX_LENGTH);

            assertThatCode(() -> new InheritedCustomizeTimeBoxDomain(Stance.PROS, speechType, "비토"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class ValidateSpeaker {

        @Test
        void 발언자_이름은_특정_글자를_초과할_수_없다() {
            String speaker = "a".repeat(CustomizeTimeBoxDomain.SPEAKER_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new InheritedCustomizeTimeBoxDomain(Stance.PROS, "비토", speaker))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEAKER_LENGTH.getMessage());
        }

        @Test
        void 발언자_이름은_비어있을_수_있다() {
            assertThatCode(() -> new InheritedCustomizeTimeBoxDomain(Stance.PROS, "비토", null))
                    .doesNotThrowAnyException();
        }
    }

    static class InheritedCustomizeTimeBoxDomain extends CustomizeTimeBoxDomain {

        protected InheritedCustomizeTimeBoxDomain(Stance stance, String speechType, String speaker) {
            super(stance, speechType, speaker);
        }

        @Override
        protected boolean isValidStance(Stance stance) {
            return true;
        }

        @Override
        public CustomizeBoxType getBoxType() {
            return null;
        }

        @Override
        public Integer getTime() {
            return 0;
        }

        @Override
        public Integer getTimePerTeam() {
            return 0;
        }

        @Override
        public Integer getTimePerSpeaking() {
            return 0;
        }
    }
}
