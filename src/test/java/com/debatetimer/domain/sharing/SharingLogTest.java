package com.debatetimer.domain.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingLogTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 10, 7, 10, 0, 0);

    private SharingLog ended(LocalDateTime endedAt) {
        return new SharingLog(1L, 1L, 2L, STARTED_AT, endedAt, 0, SharingLogStatus.FINISHED);
    }

    @Nested
    class Create {

        @Test
        void 공유_기록은_청중_없이_진행_중_상태로_시작한다() {
            SharingLog sharingLog = new SharingLog(1L, 2L, STARTED_AT);

            assertAll(
                    () -> assertThat(sharingLog.getStatus()).isEqualTo(SharingLogStatus.SHARING),
                    () -> assertThat(sharingLog.getAudienceCount()).isZero(),
                    () -> assertThat(sharingLog.getEndedAt()).isNull()
            );
        }
    }

    @Nested
    class GetDurationSeconds {

        @Test
        void 시작부터_종료까지의_시간을_초_단위로_계산한다() {
            SharingLog sharingLog = ended(STARTED_AT.plusMinutes(52).plusSeconds(7));

            assertThat(sharingLog.getDurationSeconds()).isEqualTo(52 * 60 + 7);
        }

        @Test
        void 종료되지_않은_공유는_0초이다() {
            SharingLog sharingLog = new SharingLog(1L, 2L, STARTED_AT);

            assertThat(sharingLog.getDurationSeconds()).isZero();
        }

        @Test
        void 종료_시각이_시작_시각보다_앞서면_0초이다() {
            SharingLog sharingLog = ended(STARTED_AT.minusSeconds(1));

            assertThat(sharingLog.getDurationSeconds()).isZero();
        }
    }
}
