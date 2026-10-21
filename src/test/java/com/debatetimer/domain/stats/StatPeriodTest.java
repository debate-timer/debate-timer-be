package com.debatetimer.domain.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StatPeriodTest {

    @Nested
    class Validate {

        @Test
        void 시작일이_종료일보다_늦으면_예외를_던진다() {
            LocalDate from = LocalDate.of(2026, 10, 2);
            LocalDate to = LocalDate.of(2026, 10, 1);

            assertThatThrownBy(() -> new StatPeriod(StatUnit.DAY, from, to))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_STATS_RANGE.getMessage());
        }

        @Test
        void 구간이_최대_개수를_넘으면_예외를_던진다() {
            LocalDate from = LocalDate.of(2026, 1, 1);
            LocalDate to = from.plusDays(StatPeriod.MAX_PERIOD_COUNT);

            assertThatThrownBy(() -> new StatPeriod(StatUnit.DAY, from, to))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_STATS_RANGE.getMessage());
        }

        @Test
        void 주간_단위는_일_단위보다_긴_기간을_조회할_수_있다() {
            LocalDate from = LocalDate.of(2020, 1, 1);
            LocalDate to = LocalDate.of(2026, 12, 31);

            StatPeriod period = new StatPeriod(StatUnit.WEEK, from, to);

            assertThat(period.getPeriodStarts()).hasSizeLessThanOrEqualTo(StatPeriod.MAX_PERIOD_COUNT);
        }
    }

    @Nested
    class GetPeriodStarts {

        @Test
        void 주간_단위는_월요일부터_시작하는_구간으로_나눈다() {
            // 2026-10-01 목요일 ~ 2026-10-14 수요일
            StatPeriod period = new StatPeriod(StatUnit.WEEK, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 14));

            assertAll(
                    () -> assertThat(period.getPeriodStarts()).containsExactly(
                            LocalDate.of(2026, 9, 28),
                            LocalDate.of(2026, 10, 5),
                            LocalDate.of(2026, 10, 12)
                    ),
                    () -> assertThat(period.getEndAt()).isEqualTo(LocalDate.of(2026, 10, 19).atStartOfDay())
            );
        }

        @Test
        void 월별_단위는_1일부터_시작하는_구간으로_나눈다() {
            StatPeriod period = new StatPeriod(StatUnit.MONTH, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 2));

            assertThat(period.getPeriodStarts()).containsExactly(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 3, 1)
            );
        }
    }

    @Nested
    class CountByPeriod {

        @Test
        void 시각을_구간_시작일별로_센다() {
            StatPeriod period = new StatPeriod(StatUnit.MONTH, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
            List<LocalDateTime> times = List.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 31, 23, 59),
                    LocalDateTime.of(2026, 2, 10, 12, 0),
                    LocalDateTime.of(2026, 3, 1, 0, 0)
            );

            Map<LocalDate, Long> counts = period.countByPeriod(times);

            assertThat(counts).containsExactlyInAnyOrderEntriesOf(Map.of(
                    LocalDate.of(2026, 1, 1), 2L,
                    LocalDate.of(2026, 2, 1), 1L
            ));
        }
    }
}
