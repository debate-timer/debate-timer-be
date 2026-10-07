package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ChairmanTokenExpirationTest {

    @Nested
    class SecondsFor {

        @Test
        void 토론_시간의_2배를_유효시간으로_한다() {
            long debateTime = ChairmanTokenExpiration.MIN_EXPIRATION.toSeconds();

            assertThat(ChairmanTokenExpiration.secondsFor(debateTime)).isEqualTo(debateTime * 2);
        }

        @Test
        void 토론_시간이_짧으면_최소_유효시간을_보장한다() {
            assertThat(ChairmanTokenExpiration.secondsFor(75L))
                    .isEqualTo(ChairmanTokenExpiration.MIN_EXPIRATION.toSeconds());
        }

        @Test
        void 토론_시간이_없어도_최소_유효시간을_보장한다() {
            assertThat(ChairmanTokenExpiration.secondsFor(0L))
                    .isEqualTo(ChairmanTokenExpiration.MIN_EXPIRATION.toSeconds());
        }
    }
}
