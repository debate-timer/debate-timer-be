package com.debatetimer.entity.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.sharing.SharingLogStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingLogEntityTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 10, 7, 10, 0, 0);

    @Nested
    class Create {

        @Test
        void 공유_기록은_청중_없이_진행_중_상태로_시작한다() {
            SharingLogEntity sharingLog = new SharingLogEntity(1L, 2L, STARTED_AT);

            assertAll(
                    () -> assertThat(sharingLog.getStatus()).isEqualTo(SharingLogStatus.SHARING),
                    () -> assertThat(sharingLog.getAudienceCount()).isZero(),
                    () -> assertThat(sharingLog.getEndedAt()).isNull()
            );
        }
    }

    @Nested
    class Finish {

        @Test
        void 종료하면_종료_상태와_종료_시각을_기록한다() {
            SharingLogEntity sharingLog = new SharingLogEntity(1L, 2L, STARTED_AT);

            sharingLog.finish(STARTED_AT.plusMinutes(30));

            assertAll(
                    () -> assertThat(sharingLog.getStatus()).isEqualTo(SharingLogStatus.FINISHED),
                    () -> assertThat(sharingLog.getEndedAt()).isEqualTo(STARTED_AT.plusMinutes(30))
            );
        }
    }

    @Nested
    class Abandon {

        @Test
        void 중단하면_중단_상태와_종료_시각을_기록한다() {
            SharingLogEntity sharingLog = new SharingLogEntity(1L, 2L, STARTED_AT);

            sharingLog.abandon(STARTED_AT.plusMinutes(5));

            assertAll(
                    () -> assertThat(sharingLog.getStatus()).isEqualTo(SharingLogStatus.ABANDONED),
                    () -> assertThat(sharingLog.getEndedAt()).isEqualTo(STARTED_AT.plusMinutes(5))
            );
        }
    }

    @Nested
    class GetDurationSeconds {

        @Test
        void 시작부터_종료까지의_시간을_초_단위로_계산한다() {
            SharingLogEntity sharingLog = new SharingLogEntity(1L, 2L, STARTED_AT);

            sharingLog.finish(STARTED_AT.plusMinutes(52).plusSeconds(7));

            assertThat(sharingLog.getDurationSeconds()).isEqualTo(52 * 60 + 7);
        }

        @Test
        void 종료되지_않은_공유는_0초이다() {
            SharingLogEntity sharingLog = new SharingLogEntity(1L, 2L, STARTED_AT);

            assertThat(sharingLog.getDurationSeconds()).isZero();
        }

        @Test
        void 종료_시각이_시작_시각보다_앞서면_0초이다() {
            SharingLogEntity sharingLog = new SharingLogEntity(1L, 2L, STARTED_AT);

            sharingLog.finish(STARTED_AT.minusSeconds(1));

            assertThat(sharingLog.getDurationSeconds()).isZero();
        }
    }
}
