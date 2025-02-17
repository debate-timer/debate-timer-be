package com.debatetimer.domain.parliamentary;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParliamentaryTimeBoxTest {

    @Nested
    class Validate {

        @Test
        void 순서는_양수만_가능하다() {
            ParliamentaryTable table = new ParliamentaryTable();
            assertThatThrownBy(
                    () -> new ParliamentaryTimeBox(table, 0, Stance.CONS, ParliamentaryBoxType.OPENING, 10, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE.getMessage());
        }

        @Test
        void 시간은_양수만_가능하다() {
            ParliamentaryTable table = new ParliamentaryTable();
            assertThatThrownBy(
                    () -> new ParliamentaryTimeBox(table, 1, Stance.CONS, ParliamentaryBoxType.OPENING, 0, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BOX_TIME.getMessage());
        }
    }

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
}
