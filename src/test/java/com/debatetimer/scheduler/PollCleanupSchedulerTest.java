package com.debatetimer.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.repository.poll.PollRepository;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class PollCleanupSchedulerTest extends BaseServiceTest {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollCleanupScheduler pollCleanupScheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    class CleanupStalePolls {

        @Test
        void 생성_후_일정_시간_이상_경과한_진행_상태인_투표를_완료_상태로_변경한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity poll = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            updateCreatedAt(poll.getId(), LocalDateTime.now().minusHours(PollCleanupScheduler.TIMEOUT_HOURS + 1));

            pollCleanupScheduler.cleanupStalePolls();

            PollStatus status = pollRepository.getById(poll.getId()).getStatus();
            assertThat(status).isEqualTo(PollStatus.DONE);
        }

        @Test
        void 생성_후_일정_시간_미만_경과한_진행_상태인_투표는_그대로_유지한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity poll = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            updateCreatedAt(poll.getId(), LocalDateTime.now().minusHours(PollCleanupScheduler.TIMEOUT_HOURS - 1));

            pollCleanupScheduler.cleanupStalePolls();

            PollStatus status = pollRepository.getById(poll.getId()).getStatus();
            assertThat(status).isEqualTo(PollStatus.PROGRESS);
        }

        @Test
        void 이미_완료_상태인_투표는_영향받지_않는다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity poll = pollEntityGenerator.generate(table, PollStatus.DONE);
            updateCreatedAt(poll.getId(), LocalDateTime.now().minusHours(PollCleanupScheduler.TIMEOUT_HOURS + 1));

            pollCleanupScheduler.cleanupStalePolls();

            PollStatus status = pollRepository.getById(poll.getId()).getStatus();
            assertThat(status).isEqualTo(PollStatus.DONE);
        }

        private void updateCreatedAt(Long pollId, LocalDateTime createdAt) {
            jdbcTemplate.update("UPDATE poll SET created_at = ? WHERE id = ?", createdAt, pollId);
        }
    }
}
