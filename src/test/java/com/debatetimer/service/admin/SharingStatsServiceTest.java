package com.debatetimer.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.admin.SharingDailyStatResponse;
import com.debatetimer.dto.admin.SharingStatsResponse;
import com.debatetimer.entity.sharing.SharingLogEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.sharing.SharingLogRepository;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SharingStatsServiceTest extends BaseServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    @Autowired
    private SharingStatsService sharingStatsService;

    @Autowired
    private SharingLogRepository sharingLogRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberGenerator.generate("chairman@email.com");
    }

    private void saveFinished(LocalDateTime startedAt, long minutes) {
        SharingLogEntity log = new SharingLogEntity(1L, member.getId(), startedAt);
        log.finish(startedAt.plusMinutes(minutes));
        sharingLogRepository.save(log);
    }

    private void saveAbandoned(LocalDateTime startedAt) {
        SharingLogEntity log = new SharingLogEntity(1L, member.getId(), startedAt);
        log.abandon(startedAt.plusMinutes(5));
        sharingLogRepository.save(log);
    }

    @Nested
    class GetDailyStats {

        @Test
        void 종료된_공유만_시작일별로_횟수와_평균_시간을_계산한다() {
            saveFinished(DAY.atTime(10, 0), 30);
            saveFinished(DAY.atTime(23, 59), 61);
            saveAbandoned(DAY.atTime(12, 0));
            saveFinished(DAY.plusDays(2).atTime(0, 0), 15);

            SharingStatsResponse response = sharingStatsService.getDailyStats(DAY, DAY.plusDays(2));

            assertThat(response.days()).containsExactly(
                    new SharingDailyStatResponse(DAY, 2, 45.5),
                    new SharingDailyStatResponse(DAY.plusDays(1), 0, 0),
                    new SharingDailyStatResponse(DAY.plusDays(2), 1, 15)
            );
        }

        @Test
        void 기간_밖의_공유는_포함하지_않는다() {
            saveFinished(DAY.minusDays(1).atTime(23, 59), 10);
            saveFinished(DAY.plusDays(1).atTime(0, 0), 10);

            SharingStatsResponse response = sharingStatsService.getDailyStats(DAY, DAY);

            assertAll(
                    () -> assertThat(response.days()).hasSize(1),
                    () -> assertThat(response.days().get(0).finishedCount()).isZero()
            );
        }

        @Test
        void 시작일이_종료일보다_늦으면_예외를_던진다() {
            assertThatThrownBy(() -> sharingStatsService.getDailyStats(DAY, DAY.minusDays(1)))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_STATS_PERIOD.getMessage());
        }

        @Test
        void 최대_조회_기간을_넘으면_예외를_던진다() {
            LocalDate to = DAY.plusDays(SharingStatsService.MAX_PERIOD_DAYS);

            assertThatThrownBy(() -> sharingStatsService.getDailyStats(DAY, to))
                    .isInstanceOf(DTClientErrorException.class);
        }
    }
}
