package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AgendaTest {

    @Nested
    class Validate {
        @Test
        void 테이블_이름은_정해진_길이_이내여야_한다() {
            String longAgenda = "f".repeat(Agenda.AGENDA_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new Agenda(longAgenda))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_AGENDA_LENGTH.getMessage());
        }
    }
}
