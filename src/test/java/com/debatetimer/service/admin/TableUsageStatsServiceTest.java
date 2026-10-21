package com.debatetimer.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.stats.StatUnit;
import com.debatetimer.dto.admin.TableUsageStatResponse;
import com.debatetimer.dto.admin.TableUsageStatsResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class TableUsageStatsServiceTest extends BaseServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    @Autowired
    private TableUsageStatsService tableUsageStatsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberGenerator.generate("member@email.com");
    }

    private void saveTable(LocalDateTime createdAt, LocalDateTime usedAt) {
        CustomizeTableEntity table = customizeTableRepository.save(new CustomizeTableEntity(
                new CustomizeTable(member, "테이블", "주제", "찬성", "반대", false, false, usedAt)));
        jdbcTemplate.update("UPDATE customize_table SET created_at = ? WHERE id = ?", createdAt, table.getId());
    }

    @Nested
    class GetStats {

        @Test
        void 일별로_생성_수와_사용_수를_센다() {
            saveTable(DAY.atTime(10, 0), DAY.plusDays(1).atTime(9, 0));
            saveTable(DAY.atTime(23, 59), DAY.atTime(23, 59));
            saveTable(DAY.minusDays(1).atTime(12, 0), DAY.plusDays(1).atTime(0, 0));

            TableUsageStatsResponse response = tableUsageStatsService.getStats(StatUnit.DAY, DAY, DAY.plusDays(2));

            assertThat(response.periods()).containsExactly(
                    new TableUsageStatResponse(DAY, 2, 1),
                    new TableUsageStatResponse(DAY.plusDays(1), 0, 2),
                    new TableUsageStatResponse(DAY.plusDays(2), 0, 0)
            );
        }

        @Test
        void 월별_단위는_조회_기간을_달_경계로_넓힌다() {
            saveTable(LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 9, 30, 23, 0));
            saveTable(LocalDateTime.of(2026, 10, 31, 23, 59), LocalDateTime.of(2026, 10, 31, 23, 59));

            TableUsageStatsResponse response = tableUsageStatsService.getStats(
                    StatUnit.MONTH, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 15));

            assertAll(
                    () -> assertThat(response.from()).isEqualTo(LocalDate.of(2026, 9, 1)),
                    () -> assertThat(response.to()).isEqualTo(LocalDate.of(2026, 10, 31)),
                    () -> assertThat(response.periods()).containsExactly(
                            new TableUsageStatResponse(LocalDate.of(2026, 9, 1), 1, 1),
                            new TableUsageStatResponse(LocalDate.of(2026, 10, 1), 1, 1)
                    )
            );
        }
    }
}
