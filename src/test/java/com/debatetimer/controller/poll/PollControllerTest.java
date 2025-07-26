package com.debatetimer.controller.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.dto.poll.response.PollInfoResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.poll.PollEntity;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PollControllerTest extends BaseControllerTest {

    @Nested
    class CreatePoll {

        @Test
        void 선거를_생성할_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            Headers headers = headerGenerator.generateAccessTokenHeader(member);

            given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .pathParam("tableId", table.getId())
                    .when().post("/api/polls/{tableId}")
                    .then().statusCode(HttpStatus.CREATED.value());
        }
    }

    @Nested
    class GetPollInfo {

        @Test
        void 선거정보를_읽을_수_있다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "콜리");
            voteGenerator.generate(pollEntity, VoteTeam.PROS, "비토");
            voteGenerator.generate(pollEntity, VoteTeam.CONS, "커찬");
            Headers headers = headerGenerator.generateAccessTokenHeader(member);

            PollInfoResponse response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .pathParam("pollId", pollEntity.getId())
                    .when().get("/api/polls/{pollId}")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(PollInfoResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(pollEntity.getId()),
                    () -> assertThat(response.prosTeamName()).isEqualTo(pollEntity.getProsTeamName()),
                    () -> assertThat(response.consTeamName()).isEqualTo(pollEntity.getConsTeamName()),
                    () -> assertThat(response.status()).isEqualTo(pollEntity.getStatus()),
                    () -> assertThat(response.totalCount()).isEqualTo(3L),
                    () -> assertThat(response.prosCount()).isEqualTo(2L),
                    () -> assertThat(response.consCount()).isEqualTo(1L)
            );
        }
    }

    @Nested
    class FinishPoll {

        @Test
        void 선거정보를_완료상태로_변경한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(member);
            PollEntity pollEntity = pollGenerator.generate(table, PollStatus.PROGRESS);
            Headers headers = headerGenerator.generateAccessTokenHeader(member);

            PollInfoResponse response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .pathParam("pollId", pollEntity.getId())
                    .when().patch("/api/polls/{pollId}")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(PollInfoResponse.class);

            assertThat(response.status()).isEqualTo(PollStatus.DONE);
        }
    }
}
