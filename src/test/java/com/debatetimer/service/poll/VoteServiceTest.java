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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

        @ParameterizedTest
        @EnumSource(PollStatus.class)
        void 투표자가_선거정보와_함께_시간표_정보를_조회할_수_있다(PollStatus status) {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, status);

            VoterPollInfoResponse response = voteService.getVoterPollInfo(pollEntity.getId());

            assertAll(
                    () -> assertThat(response.tableInfo().id()).isEqualTo(table.getId()),
                    () -> assertThat(response.tableInfo().name()).isEqualTo(table.getName()),
                    () -> assertThat(response.tableInfo().agenda()).isEqualTo(pollEntity.getAgenda())
            );
        }

        @Test
        void 토론_주제는_선거_생성_시점의_값을_사용한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS, "선거 생성 시점 주제");

            VoterPollInfoResponse response = voteService.getVoterPollInfo(pollEntity.getId());

            assertThat(response.tableInfo().agenda()).isEqualTo("선거 생성 시점 주제");
        }

        @Test
        void 시간표가_삭제된_선거는_조회할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            customizeTableRepository.delete(table);

            assertThatThrownBy(() -> voteService.getVoterPollInfo(pollEntity.getId()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_선거는_조회할_수_없다() {
            assertThatThrownBy(() -> voteService.getVoterPollInfo(0L))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.POLL_NOT_FOUND.getMessage());
        }
    }
}
