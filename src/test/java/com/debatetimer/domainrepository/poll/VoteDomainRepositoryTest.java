package com.debatetimer.domainrepository.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VoteDomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private VoteDomainRepository voteDomainRepository;

    @Nested
    class GetVoteInfo {

        @Test
        void 팀별_투표_현황을_알_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            voteGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");

            VoteInfo voteInfo = voteDomainRepository.findVoteInfoByPollId(pollEntity.getId());

            assertAll(
                    () -> assertThat(voteInfo.getPollId()).isEqualTo(pollEntity.getId()),
                    () -> assertThat(voteInfo.getTotalCount()).isEqualTo(3L),
                    () -> assertThat(voteInfo.getProsCount()).isEqualTo(2L),
                    () -> assertThat(voteInfo.getConsCount()).isEqualTo(1L)
            );
        }
    }

}
