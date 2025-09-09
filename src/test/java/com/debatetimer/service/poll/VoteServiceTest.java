package com.debatetimer.service.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.dto.poll.request.VoteRequest;
import com.debatetimer.dto.poll.response.VoteCreateResponse;
import com.debatetimer.dto.poll.response.VoterPollInfoResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.poll.VoteRepository;
import com.debatetimer.service.BaseServiceTest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VoteServiceTest extends BaseServiceTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private VoteRepository voteRepository;

    @Nested
    class Vote {

        @Test
        void 진행_중인_선거에_최초로_투표_할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            String participateCode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participateCode, VoteTeam.PROS);

            VoteCreateResponse response = voteService.vote(pollEntity.getId(), voteRequest);

            assertAll(
                    () -> assertThat(response.name()).isEqualTo(voteRequest.name()),
                    () -> assertThat(response.participateCode()).isEqualTo(voteRequest.participateCode()),
                    () -> assertThat(response.team()).isEqualTo(voteRequest.team())
            );
        }

        @Test
        void 이미_참여한_선거에_투표_할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            String participateCode = UUID.randomUUID().toString();
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리", participateCode);
            VoteRequest voteRequest = new VoteRequest("콜리", participateCode, VoteTeam.PROS);

            assertThatThrownBy(() -> voteService.vote(pollEntity.getId(), voteRequest))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.ALREADY_VOTED_PARTICIPANT.getMessage());
        }

        @Test
        void 투표_동시성_이슈에_단일_표만_유효하게_취급한다() throws InterruptedException {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            String participateCode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participateCode, VoteTeam.PROS);

            runAtSameTime(2, () -> voteService.vote(pollEntity.getId(), voteRequest));

            long voteCount = voteRepository.count();
            assertThat(voteCount).isEqualTo(1);
        }

        @Test
        void 끝난_선거에_투표_할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity alreadyDonePoll = pollEntityGenerator.generate(table, PollStatus.DONE);
            String participateCode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participateCode, VoteTeam.PROS);

            assertThatThrownBy(() -> voteService.vote(alreadyDonePoll.getId(), voteRequest))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.ALREADY_DONE_POLL.getMessage());
        }
    }

    @Nested
    class GetVoterPollInfo {

        @Test
        void 투표자가_선거정보를_조회할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            voteEntityGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");

            VoterPollInfoResponse response = voteService.getVoterPollInfo(pollEntity.getId());

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(pollEntity.getId()),
                    () -> assertThat(response.prosTeamName()).isEqualTo(pollEntity.getProsTeamName()),
                    () -> assertThat(response.consTeamName()).isEqualTo(pollEntity.getConsTeamName()),
                    () -> assertThat(response.status()).isEqualTo(pollEntity.getStatus()),
                    () -> assertThat(response.totalCount()).isEqualTo(3L),
                    () -> assertThat(response.participateCode()).isNotBlank(),
                    () -> assertThat(response.prosCount()).isEqualTo(2L),
                    () -> assertThat(response.consCount()).isEqualTo(1L)
            );
        }
    }
}
