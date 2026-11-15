package com.debatetimer.dto.sharing.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingRequestTest {

    @Nested
    class RecordingOpened {

        private final RecordingJoinRequest recording = new RecordingJoinRequest("session-1", "join-code");

        @Test
        void 녹음_참가_정보와_버전이_있으면_데이터_없는_이벤트로_만든다() {
            SharingRequest request = new SharingRequest(TimerEventType.RECORDING_OPENED, 100L, null, recording);

            TimerEvent timerEvent = request.toTimerEvent();

            assertThat(timerEvent.getEventType()).isEqualTo(TimerEventType.RECORDING_OPENED);
            assertThat(timerEvent.getTimerEventData()).isNull();
        }

        @Test
        void 녹음_참가_정보가_없으면_예외를_던진다() {
            SharingRequest request = new SharingRequest(TimerEventType.RECORDING_OPENED, 100L, null);

            assertThatThrownBy(request::toTimerEvent)
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIMER_EVENT.getMessage());
        }

        @Test
        void 버전이_없으면_예외를_던진다() {
            SharingRequest request = new SharingRequest(TimerEventType.RECORDING_OPENED, null, null, recording);

            assertThatThrownBy(request::toTimerEvent)
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.VERSION_REQUIRED_TIMER_EVENT.getMessage());
        }
    }

    @Nested
    class RecordingClosed {

        @Test
        void 녹음_참가_정보와_버전이_있으면_데이터_없는_이벤트로_만든다() {
            SharingRequest request = new SharingRequest(TimerEventType.RECORDING_CLOSED, 100L, null,
                    new RecordingJoinRequest("session-1", "join-code"));

            TimerEvent timerEvent = request.toTimerEvent();

            assertThat(timerEvent.getEventType()).isEqualTo(TimerEventType.RECORDING_CLOSED);
            assertThat(timerEvent.getTimerEventData()).isNull();
        }

        @Test
        void 녹음_참가_정보가_없으면_예외를_던진다() {
            SharingRequest request = new SharingRequest(TimerEventType.RECORDING_CLOSED, 100L, null);

            assertThatThrownBy(request::toTimerEvent)
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TIMER_EVENT.getMessage());
        }
    }
}
