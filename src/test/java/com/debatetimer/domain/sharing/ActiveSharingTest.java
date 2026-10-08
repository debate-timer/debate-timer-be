package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeBoxType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ActiveSharingTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 10, 7, 10, 0);
    private static final int TIME_BOX_COUNT = 3;

    private ActiveSharing activeSharing() {
        return new ActiveSharing(1L, "chairman@email.com", "테이블", TIME_BOX_COUNT, STARTED_AT);
    }

    private TimerEvent event(TimerEventType eventType, int sequence) {
        TimerEventData data = new TimerEventData(CustomizeBoxType.NORMAL, sequence, null, 30, true, null, null);
        return new TimerEvent(eventType, data);
    }

    @Nested
    class IsCompleted {

        @Test
        void 마지막_타임박스까지_진행했으면_끝까지_진행한_것이다() {
            ActiveSharing active = activeSharing().record(event(TimerEventType.PLAY, TIME_BOX_COUNT - 1));

            assertThat(active.isCompleted()).isTrue();
        }

        @Test
        void 마지막_타임박스_전이면_끝까지_진행하지_않은_것이다() {
            ActiveSharing active = activeSharing().record(event(TimerEventType.PLAY, TIME_BOX_COUNT - 2));

            assertThat(active.isCompleted()).isFalse();
        }

        @Test
        void 진행한_타임박스가_없으면_끝까지_진행하지_않은_것이다() {
            assertThat(activeSharing().isCompleted()).isFalse();
        }

        @Test
        void 다음_이벤트는_이동한_뒤_순서로_판단한다() {
            ActiveSharing active = activeSharing().record(event(TimerEventType.NEXT, TIME_BOX_COUNT - 2));

            assertThat(active.isCompleted()).isTrue();
        }

        @Test
        void 데이터가_없는_이벤트는_진행_순서를_바꾸지_않는다() {
            ActiveSharing active = activeSharing()
                    .record(event(TimerEventType.PLAY, TIME_BOX_COUNT - 1))
                    .record(new TimerEvent(TimerEventType.FINISHED));

            assertThat(active.isCompleted()).isTrue();
        }
    }

    @Nested
    class IsExpired {

        @Test
        void 연결이_끊긴_뒤_유예_시간이_지나면_만료된다() {
            LocalDateTime disconnectedAt = STARTED_AT.plusMinutes(10);
            ActiveSharing active = activeSharing().disconnect(disconnectedAt);

            assertAll(
                    () -> assertThat(active.isExpired(disconnectedAt.plus(ActiveSharing.RECONNECT_GRACE)
                            .minusSeconds(1))).isFalse(),
                    () -> assertThat(active.isExpired(disconnectedAt.plus(ActiveSharing.RECONNECT_GRACE))).isTrue()
            );
        }

        @Test
        void 다시_연결되면_만료되지_않는다() {
            LocalDateTime disconnectedAt = STARTED_AT.plusMinutes(10);
            ActiveSharing active = activeSharing().disconnect(disconnectedAt).reconnect();

            assertThat(active.isExpired(disconnectedAt.plus(ActiveSharing.RECONNECT_GRACE))).isFalse();
        }

        @Test
        void 연결이_유지돼도_너무_오래_이어지면_만료된다() {
            ActiveSharing active = activeSharing();

            assertAll(
                    () -> assertThat(active.isExpired(STARTED_AT.plus(ActiveSharing.STALE_THRESHOLD)
                            .minusSeconds(1))).isFalse(),
                    () -> assertThat(active.isExpired(STARTED_AT.plus(ActiveSharing.STALE_THRESHOLD))).isTrue()
            );
        }
    }

    @Nested
    class EndedAt {

        @Test
        void 연결이_끊긴_채_끝나면_처음_끊긴_시각을_종료_시각으로_본다() {
            LocalDateTime disconnectedAt = STARTED_AT.plusMinutes(10);
            ActiveSharing active = activeSharing()
                    .disconnect(disconnectedAt)
                    .disconnect(disconnectedAt.plusMinutes(5));

            assertThat(active.endedAt(disconnectedAt.plusMinutes(30))).isEqualTo(disconnectedAt);
        }

        @Test
        void 연결된_채_끝나면_현재_시각을_종료_시각으로_본다() {
            LocalDateTime now = STARTED_AT.plusHours(12);

            assertThat(activeSharing().endedAt(now)).isEqualTo(now);
        }
    }
}
