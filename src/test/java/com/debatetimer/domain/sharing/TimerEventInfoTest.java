package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TimerEventInfoTest {

    @Nested
    class ValidateCurrentTeam {

        @Test
        void 일반_타이머의_경우_현재_발언팀이_입력되면_안된다() {
            assertThatThrownBy(() -> new TimerEventInfo(
                    CustomizeBoxType.NORMAL,
                    2,
                    Stance.CONS,
                    30L
            )).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_NORMAL_TIMER_EVENT_INFO.getMessage());
        }

        @Test
        void 자유토론_타이머의_경우_현재_발언팀이_입력되어야_한다() {
            assertThatThrownBy(() -> new TimerEventInfo(
                    CustomizeBoxType.TIME_BASED,
                    2,
                    null,
                    30L
            )).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIMER_EVENT_INFO.getMessage());
        }
    }
}
