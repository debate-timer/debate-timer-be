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
import com.debatetimer.fixture.NullAndEmptyAndBlankSource;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.http.HttpStatus;

class VoteControllerTest extends BaseControllerTest {

    @Nested
    class GetVotersPollInfo {

        @Test
        void 투표자가_선거정보를_조회할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            voteEntityGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");

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
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            VoteRequest voteRequest = getVoteRequestBuilder().sample();

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

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 투표_시_이름은_널이거나_빈_문자열일_수_없다(String invalidName) {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            VoteRequest voteRequest = getVoteRequestBuilder()
                    .set("name", invalidName)
                    .sample();

            given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", pollEntity.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 투표_시_팀은_널일_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            VoteRequest voteRequest = getVoteRequestBuilder()
                    .set("team", null)
                    .sample();

            given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", pollEntity.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 투표_시_참여코드는_널이거나_빈_문자열일_수_없다(String participateCode) {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            VoteRequest voteRequest = getVoteRequestBuilder()
                    .set("participateCode", participateCode)
                    .sample();

            given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", pollEntity.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 이미_참여한_선거에_투표_할_수_없다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity pollEntity = pollEntityGenerator.generate(table, PollStatus.PROGRESS);
            String participatecode = UUID.randomUUID().toString();
            voteEntityGenerator.generate(pollEntity, VoteTeam.PROS, "콜리", participatecode);
            VoteRequest voteRequest = getVoteRequestBuilder()
                    .set("participateCode", participatecode)
                    .sample();

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
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            PollEntity alreadyDonePoll = pollEntityGenerator.generate(table, PollStatus.DONE);
            VoteRequest voteRequest = getVoteRequestBuilder().sample();

            given()
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", alreadyDonePoll.getId())
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}
