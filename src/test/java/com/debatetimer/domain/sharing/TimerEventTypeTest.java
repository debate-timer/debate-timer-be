package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TimerEventTypeTest {

    @Nested
    class ValidateData {

        @EnumSource(
                value = TimerEventType.class,
                names = {
                        "NEXT",
                        "BEFORE",
                        "STOP",
                        "PLAY",
                        "RESET",
                        "TEAM_SWITCH",
                }
        )
        @ParameterizedTest
        void 타이머_이벤트_데이터가_존재하여야_한다(TimerEventType eventType) {
            assertThatThrownBy(() -> eventType.validateEventData(null))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIMER_EVENT.getMessage());

        }

        @EnumSource(value = TimerEventType.class, names = {"FINISHED"})
        @ParameterizedTest
        void 타이머_이벤트_데이터가_존재하지_않아야_한다(TimerEventType eventType) {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.NORMAL,
                    2,
                    null,
                    30L
            );
            assertThatThrownBy(() -> eventType.validateEventData(timerEventData))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIMER_EVENT.getMessage());
        }
    }
}
