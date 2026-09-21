package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TimerEventTest {

    @Nested
    class Validate {

        @Test
        void 동기화_이벤트는_재생_여부가_없으면_생성할_수_없다() {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.NORMAL,
                    2,
                    null,
                    142L,
                    null,
                    null,
                    null
            );

            assertThatThrownBy(() -> new TimerEvent(TimerEventType.SYNC, timerEventData))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_SYNC_TIMER_EVENT_DATA.getMessage());
        }

        @Test
        void 자유토론_동기화_이벤트는_양_팀의_남은_시간이_없으면_생성할_수_없다() {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.TIME_BASED,
                    4,
                    Stance.CONS,
                    20L,
                    false,
                    45L,
                    null
            );

            assertThatThrownBy(() -> new TimerEvent(TimerEventType.SYNC, timerEventData))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_SYNC_TIMER_EVENT_DATA.getMessage());
        }

        @Test
        void 동기화_이벤트는_데이터가_없으면_생성할_수_없다() {
            assertThatThrownBy(() -> new TimerEvent(TimerEventType.SYNC))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIMER_EVENT.getMessage());
        }

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
        void 동기화_이벤트가_아니면_새_필드가_없어도_생성할_수_있다(TimerEventType eventType) {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.TIME_BASED,
                    4,
                    Stance.CONS,
                    20L,
                    null,
                    null,
                    null
            );

            assertThatCode(() -> new TimerEvent(eventType, timerEventData))
                    .doesNotThrowAnyException();
        }
    }
}
