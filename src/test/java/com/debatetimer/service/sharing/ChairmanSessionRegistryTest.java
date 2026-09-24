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

class ChairmanSessionRegistryTest {

    private static final long ROOM_ID = 1L;

    private MutableClock clock;
    private ChairmanSessionRegistry chairmanSessionRegistry;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-09-24T10:00:00Z"));
        chairmanSessionRegistry = new ChairmanSessionRegistry(clock);
    }

    @Nested
    class Claim {

        @Test
        void 사회자가_없는_룸에서는_새로운_사회자로_등록된다() {
            ChairmanClaim claim = chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");

            assertAll(
                    () -> assertThat(claim).isEqualTo(ChairmanClaim.NEW),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isTrue(),
                    () -> assertThat(chairmanSessionRegistry.hasActiveChairman(ROOM_ID)).isTrue()
            );
        }

        @Test
        void 활성_사회자가_다시_요청하면_그대로_유지된다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");

            ChairmanClaim claim = chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-2");

            assertAll(
                    () -> assertThat(claim).isEqualTo(ChairmanClaim.SAME),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isTrue()
            );
        }

        @Test
        void 다른_사회자가_요청하면_최신_사회자가_활성_세션을_넘겨받는다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");

            ChairmanClaim claim = chairmanSessionRegistry.claim(ROOM_ID, "tab-b", "simp-2");

            assertAll(
                    () -> assertThat(claim).isEqualTo(ChairmanClaim.TAKEOVER),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-b")).isTrue(),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isFalse(),
                    () -> assertThat(chairmanSessionRegistry.findActiveSessionId(ROOM_ID)).contains("tab-b")
            );
        }

        @Test
        void 밀려난_사회자는_다시_활성_세션을_가져올_수_없다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            chairmanSessionRegistry.claim(ROOM_ID, "tab-b", "simp-2");

            ChairmanClaim claim = chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-3");

            assertAll(
                    () -> assertThat(claim).isEqualTo(ChairmanClaim.REJECTED),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-b")).isTrue()
            );
        }

        @Test
        void 연결이_끊긴_사회자_대신_다른_사회자가_등록되면_이전_사회자는_돌아올_수_없다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            chairmanSessionRegistry.release("simp-1");
            chairmanSessionRegistry.claim(ROOM_ID, "tab-b", "simp-2");

            ChairmanClaim claim = chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-3");

            assertThat(claim).isEqualTo(ChairmanClaim.REJECTED);
        }

        @Test
        void 연결이_끊겼던_사회자가_재연결하면_다시_활성_사회자가_된다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            chairmanSessionRegistry.release("simp-1");

            ChairmanClaim claim = chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-2");

            assertAll(
                    () -> assertThat(claim).isEqualTo(ChairmanClaim.SAME),
                    () -> assertThat(chairmanSessionRegistry.hasActiveChairman(ROOM_ID)).isTrue()
            );
        }

        @Test
        void 룸마다_활성_사회자를_따로_관리한다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");

            ChairmanClaim claim = chairmanSessionRegistry.claim(2L, "tab-b", "simp-2");

            assertAll(
                    () -> assertThat(claim).isEqualTo(ChairmanClaim.NEW),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isTrue()
            );
        }
    }

    @Nested
    class Release {

        @Test
        void 활성_사회자의_연결이_끊기면_룸에_활성_사회자가_없다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");

            chairmanSessionRegistry.release("simp-1");

            assertAll(
                    () -> assertThat(chairmanSessionRegistry.hasActiveChairman(ROOM_ID)).isFalse(),
                    () -> assertThat(chairmanSessionRegistry.findActiveSessionId(ROOM_ID)).isEmpty(),
                    () -> assertThat(chairmanSessionRegistry.findLatestSessionId(ROOM_ID)).contains("tab-a")
            );
        }

        @Test
        void 재연결_전의_옛_연결이_늦게_끊겨도_활성_사회자를_유지한다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-2");

            chairmanSessionRegistry.release("simp-1");

            assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isTrue();
        }

        @Test
        void 밀려난_사회자의_연결이_끊겨도_최신_사회자를_유지한다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            chairmanSessionRegistry.claim(ROOM_ID, "tab-b", "simp-2");

            chairmanSessionRegistry.release("simp-1");

            assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-b")).isTrue();
        }
    }

    @Nested
    class RemoveExpired {

        @Test
        void 유지_시간이_지나면_밀려난_사회자_기록을_지운다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            chairmanSessionRegistry.claim(ROOM_ID, "tab-b", "simp-2");
            chairmanSessionRegistry.release("simp-2");
            clock.advance(ChairmanSessionRegistry.SESSION_TTL);

            chairmanSessionRegistry.removeExpired();

            assertThat(chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-3")).isEqualTo(ChairmanClaim.NEW);
        }

        @Test
        void 연결된_활성_사회자는_유지_시간이_지나도_지우지_않는다() {
            chairmanSessionRegistry.claim(ROOM_ID, "tab-a", "simp-1");
            clock.advance(ChairmanSessionRegistry.SESSION_TTL);

            chairmanSessionRegistry.removeExpired();

            assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isTrue();
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
