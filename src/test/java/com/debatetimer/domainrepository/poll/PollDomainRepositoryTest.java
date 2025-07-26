package com.debatetimer.domainrepository.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PollDomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private PollDomainRepository pollDomainRepository;

    @Nested
    class Create {

        @Test
        void 선거를_생성한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            Poll poll = new Poll(table.getId(), member.getId(), "찬성", "반대", "주제");

            Poll createdPoll = pollDomainRepository.create(poll);

            Optional<PollEntity> foundPollEntity = pollRepository.findById(createdPoll.getId());
            assertThat(foundPollEntity).isPresent();
        }
    }

    @Nested
    class GetByIdAndMemberId {

        @Test
        void 회원이_개최한_선거를_가져온다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);

            Poll foundPoll = pollDomainRepository.getByIdAndMemberId(pollEntity.getId(), member.getId());

            assertAll(
                    () -> assertThat(foundPoll.getId()).isEqualTo(pollEntity.getId()),
                    () -> assertThat(foundPoll.getAgenda().getValue()).isEqualTo(pollEntity.getAgenda()),
                    () -> assertThat(foundPoll.getStatus()).isEqualTo(pollEntity.getStatus()),
                    () -> assertThat(foundPoll.getMemberId()).isEqualTo(pollEntity.getMemberId()),
                    () -> assertThat(foundPoll.getProsTeamName().getValue()).isEqualTo(pollEntity.getProsTeamName()),
                    () -> assertThat(foundPoll.getConsTeamName().getValue()).isEqualTo(pollEntity.getConsTeamName())
            );
        }
    }

    @Nested
    class FinishPoll {

        @Test
        void 선거를_완료_상태로_변경한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);

            Poll updatedPoll = pollDomainRepository.finishPoll(pollEntity.getId(), member.getId());

            assertThat(updatedPoll.getStatus()).isEqualTo(PollStatus.DONE);
        }
    }
}
