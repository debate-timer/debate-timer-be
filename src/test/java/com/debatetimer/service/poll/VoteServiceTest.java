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
import com.debatetimer.service.BaseServiceTest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VoteServiceTest extends BaseServiceTest {

    @Autowired
    private VoteService voteService;

    @Nested
    class Vote {

        @Test
        void 진행_중인_선거에_최초로_투표_할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            String participatecode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participatecode, VoteTeam.PROS);

            VoteCreateResponse response = voteService.vote(pollEntity.getId(), voteRequest);

            assertAll(
                    () -> assertThat(response.name()).isEqualTo(voteRequest.name()),
                    () -> assertThat(response.participantCode()).isEqualTo(voteRequest.participateCode()),
                    () -> assertThat(response.team()).isEqualTo(voteRequest.team())
            );
        }

        @Test
        void 이미_참여한_선거에_투표_할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            String participatecode = UUID.randomUUID().toString();
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리", participatecode);
            VoteRequest voteRequest = new VoteRequest("콜리", participatecode, VoteTeam.PROS);

            assertThatThrownBy(() -> voteService.vote(pollEntity.getId(), voteRequest))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.ALREADY_VOTED_PARTICIPANT.getMessage());
        }

        @Test
        void 끝난_선거에_투표_할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity alreadyDonePoll = pollGenerator.generate(table, PollStatus.DONE);
            String participatecode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participatecode, VoteTeam.PROS);

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
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            voteGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");

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
