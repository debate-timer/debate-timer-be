package com.debatetimer.service.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.dto.poll.response.PollCreateResponse;
import com.debatetimer.dto.poll.response.PollInfoResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.entity.poll.VoteEntity;
import com.debatetimer.service.BaseServiceTest;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PollServiceTest extends BaseServiceTest {

    @Autowired
    private PollService pollService;

    @Nested
    class CreatePoll {

        @Test
        void 선거를_생성한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);

            PollCreateResponse createdPoll = pollService.create(table.getId(), member);

            Optional<PollEntity> foundPoll = pollRepository.findById(createdPoll.id());
            assertThat(foundPoll).isPresent();
        }
    }

    @Nested
    class GetPollInfo {

        @Test
        void 선거_정보를_읽어온다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            VoteEntity voter1 = voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            VoteEntity voter2 = voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            VoteEntity voter3 = voteEntityGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");

            PollInfoResponse pollInfo = pollService.getPollInfo(table.getId(), member);

            assertAll(
                    () -> assertThat(pollInfo.id()).isEqualTo(pollEntity.getId()),
                    () -> assertThat(pollInfo.prosTeamName()).isEqualTo(pollEntity.getProsTeamName()),
                    () -> assertThat(pollInfo.consTeamName()).isEqualTo(pollEntity.getConsTeamName()),
                    () -> assertThat(pollInfo.status()).isEqualTo(pollEntity.getStatus()),
                    () -> assertThat(pollInfo.totalCount()).isEqualTo(3L),
                    () -> assertThat(pollInfo.prosCount()).isEqualTo(2L),
                    () -> assertThat(pollInfo.consCount()).isEqualTo(1L),
                    () -> assertThat(pollInfo.voterNames()).containsExactly(
                            voter1.getName(), voter2.getName(), voter3.getName())
            );
        }
    }

    @Nested
    class FinishPoll {

        @Test
        void 선거를_완료상태로_변경한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            pollEntityGenerator.generate(table, PollStatus.PROGRESS);

            PollInfoResponse pollInfo = pollService.finishPoll(table.getId(), member);

            assertThat(pollInfo.status()).isEqualTo(PollStatus.DONE);
        }
    }
}
