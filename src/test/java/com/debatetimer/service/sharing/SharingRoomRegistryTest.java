package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    class AcceptVersion {

        @Test
        void 처음_받은_버전은_수락한다() {
            assertThat(sharingRoomRegistry.acceptVersion(1L, 100L)).isTrue();
        }

        @Test
        void 마지막_버전보다_큰_버전은_수락한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);

            assertThat(sharingRoomRegistry.acceptVersion(1L, 101L)).isTrue();
        }

        @Test
        void 마지막_버전과_같은_버전은_중복으로_보고_거절한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);

            assertThat(sharingRoomRegistry.acceptVersion(1L, 100L)).isFalse();
        }

        @Test
        void 마지막_버전보다_작은_버전은_오래된_이벤트로_보고_거절한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);

            assertThat(sharingRoomRegistry.acceptVersion(1L, 99L)).isFalse();
        }

        @Test
        void 거절된_버전은_마지막_버전을_바꾸지_않는다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);
            sharingRoomRegistry.acceptVersion(1L, 99L);

            assertThat(sharingRoomRegistry.acceptVersion(1L, 100L)).isFalse();
        }

        @Test
        void 버전이_없는_이벤트는_하위_호환을_위해_항상_수락한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);

            assertAll(
                    () -> assertThat(sharingRoomRegistry.acceptVersion(1L, null)).isTrue(),
                    () -> assertThat(sharingRoomRegistry.acceptVersion(1L, 101L)).isTrue()
            );
        }

        @Test
        void 룸마다_버전을_따로_관리한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);

            assertThat(sharingRoomRegistry.acceptVersion(2L, 50L)).isTrue();
        }

        @Test
        void 버전을_초기화하면_작은_버전도_수락한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);

            sharingRoomRegistry.resetVersion(1L);

            assertThat(sharingRoomRegistry.acceptVersion(1L, 50L)).isTrue();
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

    @Nested
    class RunExclusively {

        @Test
        void 같은_룸의_작업은_동시에_실행되지_않는다() throws InterruptedException {
            CountDownLatch firstStarted = new CountDownLatch(1);
            CountDownLatch releaseFirst = new CountDownLatch(1);
            AtomicBoolean secondRanWhileFirstHeld = new AtomicBoolean(false);
            AtomicBoolean firstFinished = new AtomicBoolean(false);

            Thread first = new Thread(() -> sharingRoomRegistry.runExclusively(1L, () -> {
                firstStarted.countDown();
                await(releaseFirst);
                firstFinished.set(true);
            }));
            first.start();
            firstStarted.await(1, TimeUnit.SECONDS);

            Thread second = new Thread(() -> sharingRoomRegistry.runExclusively(1L,
                    () -> secondRanWhileFirstHeld.set(!firstFinished.get())));
            second.start();
            second.join(300);
            releaseFirst.countDown();
            first.join(1000);
            second.join(1000);

            assertThat(secondRanWhileFirstHeld).isFalse();
        }

        @Test
        void 버전_초기화도_같은_룸의_작업이_끝난_뒤_실행된다() throws InterruptedException {
            CountDownLatch firstStarted = new CountDownLatch(1);
            CountDownLatch releaseFirst = new CountDownLatch(1);

            Thread first = new Thread(() -> sharingRoomRegistry.runExclusively(1L, () -> {
                sharingRoomRegistry.acceptVersion(1L, 100L);
                firstStarted.countDown();
                await(releaseFirst);
            }));
            first.start();
            firstStarted.await(1, TimeUnit.SECONDS);

            Thread reset = new Thread(() -> sharingRoomRegistry.resetVersion(1L));
            reset.start();
            reset.join(300);
            boolean resetBlocked = reset.isAlive();
            releaseFirst.countDown();
            first.join(1000);
            reset.join(1000);

            assertAll(
                    () -> assertThat(resetBlocked).isTrue(),
                    () -> assertThat(sharingRoomRegistry.acceptVersion(1L, 50L)).isTrue()
            );
        }

        private void await(CountDownLatch latch) {
            try {
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    class RemoveExpiredVersion {

        @Test
        void 마지막_수락_후_유지_시간이_지난_버전을_제거한다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);
            clock.advance(SharingRoomRegistry.VERSION_TTL);

            sharingRoomRegistry.removeExpired();

            assertThat(sharingRoomRegistry.acceptVersion(1L, 50L)).isTrue();
        }

        @Test
        void 유지_시간이_지나지_않은_버전은_제거하지_않는다() {
            sharingRoomRegistry.acceptVersion(1L, 100L);
            clock.advance(SharingRoomRegistry.VERSION_TTL.minusSeconds(1));

            sharingRoomRegistry.removeExpired();

            assertThat(sharingRoomRegistry.acceptVersion(1L, 50L)).isFalse();
        }
    }

    @Nested
    class TryAcquireSyncRequest {

        @Test
        void 처음_요청은_허용한다() {
            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(1L)).isTrue();
        }

        @Test
        void 최소_간격_안의_뒤따르는_요청은_거절한다() {
            sharingRoomRegistry.tryAcquireSyncRequest(1L);

            clock.advance(SharingRoomRegistry.SYNC_REQUEST_INTERVAL.minusMillis(1));

            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(1L)).isFalse();
        }

        @Test
        void 같은_시각의_연속_요청은_거절한다() {
            sharingRoomRegistry.tryAcquireSyncRequest(1L);

            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(1L)).isFalse();
        }

        @Test
        void 최소_간격이_지난_요청은_허용한다() {
            sharingRoomRegistry.tryAcquireSyncRequest(1L);

            clock.advance(SharingRoomRegistry.SYNC_REQUEST_INTERVAL);

            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(1L)).isTrue();
        }

        @Test
        void 거절된_요청은_마지막_요청_시각을_바꾸지_않는다() {
            sharingRoomRegistry.tryAcquireSyncRequest(1L);
            clock.advance(SharingRoomRegistry.SYNC_REQUEST_INTERVAL.minusMillis(1));
            sharingRoomRegistry.tryAcquireSyncRequest(1L);

            clock.advance(Duration.ofMillis(1));

            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(1L)).isTrue();
        }

        @Test
        void 룸마다_요청_간격을_따로_관리한다() {
            sharingRoomRegistry.tryAcquireSyncRequest(1L);

            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(2L)).isTrue();
        }

        @Test
        void 유지_시간이_지난_요청_기록을_제거한다() {
            sharingRoomRegistry.tryAcquireSyncRequest(1L);
            clock.advance(SharingRoomRegistry.SYNC_REQUEST_TTL);

            sharingRoomRegistry.removeExpired();
            clock.advance(SharingRoomRegistry.SYNC_REQUEST_TTL.negated().plusMillis(1)); // 제거되지 않았다면 거절되는 시점

            assertThat(sharingRoomRegistry.tryAcquireSyncRequest(1L)).isTrue();
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
