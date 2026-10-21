package com.debatetimer.service.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.stats.StatUnit;
import com.debatetimer.dto.admin.MemberSignupStatResponse;
import com.debatetimer.dto.admin.MemberSignupStatsResponse;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class MemberSignupStatsServiceTest extends BaseServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    @Autowired
    private MemberSignupStatsService memberSignupStatsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int sequence = 0;

    private void saveMember(LocalDateTime createdAt) {
        Member member = memberGenerator.generate("member%d@email.com".formatted(sequence++));
        jdbcTemplate.update("UPDATE member SET created_at = ? WHERE id = ?", createdAt, member.getId());
    }

    @Nested
    class GetStats {

        @Test
        void 구간별_신규_회원_수와_누적_회원_대비_증가율을_계산한다() {
            saveMember(DAY.minusDays(1).atTime(10, 0));
            saveMember(DAY.minusDays(1).atTime(11, 0));
            saveMember(DAY.atTime(0, 0));
            saveMember(DAY.plusDays(1).atTime(12, 0));
            saveMember(DAY.plusDays(1).atTime(23, 59));

            MemberSignupStatsResponse response = memberSignupStatsService.getStats(StatUnit.DAY, DAY, DAY.plusDays(2));

            assertThat(response.periods()).containsExactly(
                    new MemberSignupStatResponse(DAY, 1, 3, 50.0),
                    new MemberSignupStatResponse(DAY.plusDays(1), 2, 5, 66.7),
                    new MemberSignupStatResponse(DAY.plusDays(2), 0, 5, 0.0)
            );
        }

        @Test
        void 구간_시작_전_회원이_없으면_증가율을_비운다() {
            saveMember(DAY.atTime(9, 0));

            MemberSignupStatsResponse response = memberSignupStatsService.getStats(StatUnit.MONTH, DAY, DAY);

            assertThat(response.periods()).containsExactly(
                    new MemberSignupStatResponse(DAY, 1, 1, null)
            );
        }
    }
}
