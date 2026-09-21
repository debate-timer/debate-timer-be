package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingRoomRegistryTest {

    private MutableClock clock;
    private SharingRoomRegistry sharingRoomRegistry;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-09-22T10:00:00Z"));
        sharingRoomRegistry = new SharingRoomRegistry(clock);
    }

    @Nested
    class IsFinished {

        @Test
        void 종료_처리된_룸은_종료_상태이다() {
            sharingRoomRegistry.markFinished(1L);

            assertThat(sharingRoomRegistry.isFinished(1L)).isTrue();
        }

        @Test
        void 종료_처리되지_않은_룸은_종료_상태가_아니다() {
            sharingRoomRegistry.markFinished(1L);

            assertThat(sharingRoomRegistry.isFinished(2L)).isFalse();
        }

        @Test
        void 종료_유지_시간_직전까지는_종료_상태이다() {
            sharingRoomRegistry.markFinished(1L);

            clock.advance(SharingRoomRegistry.FINISHED_TTL.minusSeconds(1));

            assertThat(sharingRoomRegistry.isFinished(1L)).isTrue();
        }

        @Test
        void 종료_유지_시간이_지나면_종료_상태가_아니다() {
            sharingRoomRegistry.markFinished(1L);

            clock.advance(SharingRoomRegistry.FINISHED_TTL);

            assertThat(sharingRoomRegistry.isFinished(1L)).isFalse();
        }

        @Test
        void 다시_종료_처리하면_종료_유지_시간이_갱신된다() {
            sharingRoomRegistry.markFinished(1L);
            clock.advance(SharingRoomRegistry.FINISHED_TTL.minusSeconds(1));

            sharingRoomRegistry.markFinished(1L);
            clock.advance(Duration.ofSeconds(1));

            assertThat(sharingRoomRegistry.isFinished(1L)).isTrue();
        }
    }

    @Nested
    class Reopen {

        @Test
        void 종료된_룸을_다시_열면_종료_상태가_아니다() {
            sharingRoomRegistry.markFinished(1L);

            sharingRoomRegistry.reopen(1L);

            assertThat(sharingRoomRegistry.isFinished(1L)).isFalse();
        }

        @Test
        void 종료되지_않은_룸을_다시_열어도_예외가_발생하지_않는다() {
            sharingRoomRegistry.reopen(1L);

            assertThat(sharingRoomRegistry.isFinished(1L)).isFalse();
        }
    }

    @Nested
    class RemoveExpired {

        @Test
        void 종료_유지_시간이_지난_룸만_제거한다() {
            sharingRoomRegistry.markFinished(1L);
            clock.advance(Duration.ofHours(1));
            sharingRoomRegistry.markFinished(2L);
            clock.advance(SharingRoomRegistry.FINISHED_TTL.minusMinutes(30));

            sharingRoomRegistry.removeExpired();
            clock.advance(Duration.ofHours(-1)); // 제거되지 않았다면 다시 종료 상태로 보이는 시점

            assertAll(
                    () -> assertThat(sharingRoomRegistry.isFinished(1L)).isFalse(),
                    () -> assertThat(sharingRoomRegistry.isFinished(2L)).isTrue()
            );
        }
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
