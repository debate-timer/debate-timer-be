package com.debatetimer.controller.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.dto.poll.request.VoteRequest;
import com.debatetimer.dto.poll.response.VoteCreateResponse;
import com.debatetimer.dto.poll.response.VoterPollInfoResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class VoteControllerTest extends BaseControllerTest {

    @Nested
    class GetVotersPollInfo {

        @Test
        void 투표자가_선거정보를_조회할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            voteGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");

            VoterPollInfoResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("pollId", pollEntity.getId())
                    .when().get("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(VoterPollInfoResponse.class);

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

    @Nested
    class VotePoll {

        @Test
        void 진행_중인_선거에_최초로_투표_할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            String participatecode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participatecode, VoteTeam.PROS);

            VoteCreateResponse response = given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", pollEntity.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .extract().as(VoteCreateResponse.class);

            assertAll(
                    () -> assertThat(response.name()).isEqualTo(voteRequest.name()),
                    () -> assertThat(response.participateCode()).isEqualTo(voteRequest.participateCode()),
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

            given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", pollEntity.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 끝난_선거에_투표_할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity alreadyDonePoll = pollGenerator.generate(table, PollStatus.DONE);
            String participatecode = UUID.randomUUID().toString();
            VoteRequest voteRequest = new VoteRequest("콜리", participatecode, VoteTeam.PROS);

            given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", alreadyDonePoll.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}
