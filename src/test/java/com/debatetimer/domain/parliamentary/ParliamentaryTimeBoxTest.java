package com.debatetimer.domain.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParliamentaryTimeBoxTest {

    @Nested
    class ValidateStance {

        @Test
        void 박스타입에_가능한_입장을_검증한다() {
            ParliamentaryTable table = new ParliamentaryTable();
            assertThatCode(() -> new ParliamentaryTimeBox(table, 1, Stance.CONS, ParliamentaryBoxType.OPENING, 10, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        void 박스타입에_불가한_입장으로_생성을_시도하면_예외를_발생시킨다() {
            ParliamentaryTable table = new ParliamentaryTable();
            assertThatThrownBy(
                    () -> new ParliamentaryTimeBox(table, 1, Stance.NEUTRAL, ParliamentaryBoxType.OPENING, 10, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_STANCE.getMessage());
        }
    }

    @Nested
    class ValidateSpeakerNumber {

        @ValueSource(ints = {0, -1})
        @ParameterizedTest
        void 의회식_타임박스의_발표자_번호_음수는_불가능하다(int speakerNumber) {
            ParliamentaryTable table = new ParliamentaryTable();

            assertThatThrownBy(
                    () -> new ParliamentaryTimeBox(table, 1, Stance.PROS, ParliamentaryBoxType.OPENING, 10, speakerNumber))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SPEAKER.getMessage());
        }
    }

    @Nested
    class getSpeakerNumber {

        @ValueSource(ints = {1, 5})
        @ParameterizedTest
        void 의회식_타임박스의_발표자_번호는_양수만_가능하다(int speakerNumber) {
            ParliamentaryTable table = new ParliamentaryTable();
            ParliamentaryTimeBox timeBox = new ParliamentaryTimeBox(table, 1, Stance.PROS, ParliamentaryBoxType.OPENING, 10, speakerNumber);

            Integer actual = timeBox.getSpeakerNumber();

            assertThat(actual).isEqualTo(speakerNumber);
        }

        @Test
        void 의회식_타임박스의_발표자는_비어있을_수_있다() {
            ParliamentaryTable table = new ParliamentaryTable();
            Integer speakerNumber = null;
            ParliamentaryTimeBox timeBox = new ParliamentaryTimeBox(table, 1, Stance.PROS, ParliamentaryBoxType.OPENING, 10, speakerNumber);

            Integer actual = timeBox.getSpeakerNumber();

            assertThat(actual).isEqualTo(speakerNumber);
        }
    }
}
