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
import org.junit.jupiter.params.provider.CsvSource;

class TimerEventDataTest {

    @Nested
    class ValidateCurrentTeam {

        @Test
        void 일반_타이머의_경우_현재_발언팀이_입력되면_안된다() {
            assertThatThrownBy(() -> new TimerEventData(
                    CustomizeBoxType.NORMAL,
                    2,
                    Stance.CONS,
                    30L,
                    null,
                    null,
                    null
            )).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_NORMAL_TIMER_EVENT_DATA.getMessage());
        }

        @Test
        void 자유토론_타이머의_경우_현재_발언팀이_입력되어야_한다() {
            assertThatThrownBy(() -> new TimerEventData(
                    CustomizeBoxType.TIME_BASED,
                    2,
                    null,
                    30L,
                    null,
                    null,
                    null
            )).isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIME_BASED_TIMER_EVENT_DATA.getMessage());
        }
    }

    @Nested
    class ValidateSyncable {

        @Test
        void 동기화_데이터는_재생_여부가_입력되어야_한다() {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.NORMAL,
                    2,
                    null,
                    30L,
                    null,
                    null,
                    null
            );

            assertThatThrownBy(timerEventData::validateSyncable)
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_SYNC_TIMER_EVENT_DATA.getMessage());
        }

        @Test
        void 일반_타이머는_재생_여부만_있으면_동기화할_수_있다() {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.NORMAL,
                    2,
                    null,
                    30L,
                    true,
                    null,
                    null
            );

            assertThatCode(timerEventData::validateSyncable)
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @CsvSource(value = {"null, 38", "45, null", "null, null"}, nullValues = "null")
        void 자유토론_타이머는_양_팀의_남은_시간이_입력되어야_한다(Long prosRemainingTime, Long consRemainingTime) {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.TIME_BASED,
                    4,
                    Stance.CONS,
                    20L,
                    false,
                    prosRemainingTime,
                    consRemainingTime
            );

            assertThatThrownBy(timerEventData::validateSyncable)
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_SYNC_TIMER_EVENT_DATA.getMessage());
        }

        @Test
        void 자유토론_타이머는_재생_여부와_양_팀의_남은_시간이_있으면_동기화할_수_있다() {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.TIME_BASED,
                    4,
                    Stance.CONS,
                    20L,
                    false,
                    45L,
                    38L
            );

            assertThatCode(timerEventData::validateSyncable)
                    .doesNotThrowAnyException();
        }
    }
}
