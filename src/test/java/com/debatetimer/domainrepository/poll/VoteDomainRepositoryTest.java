package com.debatetimer.domainrepository.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.ParticipateCode;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.Vote;
import com.debatetimer.domain.poll.VoteInfo;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.UUID;
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

    @Nested
    class isExists {

        @Test
        void 이미_참여한_투표인지_알_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity alreadyParticipatedPoll = pollGenerator.generate(table, PollStatus.PROGRESS);
            PollEntity notYetParticipatedPoll = pollGenerator.generate(table, PollStatus.PROGRESS);
            ParticipateCode participateCode = new ParticipateCode(UUID.randomUUID().toString());
            voteGenerator.generate(alreadyParticipatedPoll, VoteTeam.PROS, "콜리", participateCode.getValue());

            boolean participated = voteDomainRepository.isExists(alreadyParticipatedPoll.getId(), participateCode);
            boolean notYetParticipated = voteDomainRepository.isExists(notYetParticipatedPoll.getId(),
                    participateCode);

            assertAll(
                    () -> assertThat(participated).isTrue(),
                    () -> assertThat(notYetParticipated).isFalse()
            );
        }
    }

    @Nested
    class Save {

        @Test
        void 투표할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            Vote vote = new Vote(pollEntity.getId(), VoteTeam.PROS, "콜리", UUID.randomUUID().toString());

            Vote savedVote = voteDomainRepository.save(vote);

            assertAll(
                    () -> assertThat(savedVote.getName().getValue()).isEqualTo(vote.getName().getValue()),
                    () -> assertThat(savedVote.getCode().getValue()).isEqualTo(vote.getCode().getValue()),
                    () -> assertThat(savedVote.getTeam()).isEqualTo(vote.getTeam())
            );
        }

        @Test
        void 중복_투표할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            String participateCode = UUID.randomUUID().toString();
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리", participateCode);
            Vote vote = new Vote(pollEntity.getId(), VoteTeam.PROS, "콜리", participateCode);

            assertThatThrownBy(() -> voteDomainRepository.save(vote))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.ALREADY_VOTED_PARTICIPANT.getMessage());
        }
    }
}
