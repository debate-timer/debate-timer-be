package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
                        "SYNC",
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
                    30L,
                    null,
                    null,
                    null
            );
            assertThatThrownBy(() -> eventType.validateEventData(timerEventData))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIMER_EVENT.getMessage());
        }

        @Test
        void 사회자_부재_이벤트는_서버_전용이라_데이터와_관계없이_검증에_실패한다() {
            TimerEventData timerEventData = new TimerEventData(
                    CustomizeBoxType.NORMAL,
                    2,
                    null,
                    30L,
                    null,
                    null,
                    null
            );
            assertAll(
                    () -> assertThatThrownBy(() -> TimerEventType.CHAIRMAN_ABSENT.validateEventData(null))
                            .isInstanceOf(DTClientErrorException.class),
                    () -> assertThatThrownBy(() -> TimerEventType.CHAIRMAN_ABSENT.validateEventData(timerEventData))
                            .isInstanceOf(DTClientErrorException.class)
            );
        }
    }

    @Nested
    class IsSync {

        @Test
        void 동기화_이벤트인지_판단한다() {
            assertThat(TimerEventType.SYNC.isSync()).isTrue();
        }

        @EnumSource(value = TimerEventType.class, names = {"SYNC"}, mode = EnumSource.Mode.EXCLUDE)
        @ParameterizedTest
        void 동기화_이벤트가_아니면_거짓을_반환한다(TimerEventType eventType) {
            assertThat(eventType.isSync()).isFalse();
        }
    }

    @Nested
    class ToDisplayedSequence {

        @Test
        void 다음_이벤트는_이동한_뒤_순서를_구한다() {
            assertThat(TimerEventType.NEXT.toDisplayedSequence(1)).isEqualTo(2);
        }

        @Test
        void 이전_이벤트는_이동한_뒤_순서를_구한다() {
            assertThat(TimerEventType.BEFORE.toDisplayedSequence(1)).isZero();
        }

        @Test
        void 첫_순서에서_이전_이벤트가_와도_첫_순서를_유지한다() {
            assertThat(TimerEventType.BEFORE.toDisplayedSequence(0)).isZero();
        }

        @EnumSource(value = TimerEventType.class, names = {"NEXT", "BEFORE"}, mode = EnumSource.Mode.EXCLUDE)
        @ParameterizedTest
        void 이동하지_않는_이벤트는_순서를_그대로_쓴다(TimerEventType eventType) {
            assertThat(eventType.toDisplayedSequence(1)).isEqualTo(1);
        }
    }
}
