package com.debatetimer.controller.timebased;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.dto.timebased.request.TimeBasedTableCreateRequest;
import com.debatetimer.dto.timebased.request.TimeBasedTableInfoCreateRequest;
import com.debatetimer.dto.timebased.request.TimeBasedTimeBoxCreateRequest;
import com.debatetimer.dto.timebased.response.TimeBasedTableResponse;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TimeBasedControllerTest extends BaseControllerTest {

    @Nested
    class Save {

        @Test
        void 시간총량제_테이블을_생성한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            TimeBasedTableCreateRequest request = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("비토 테이블", "주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            TimeBasedTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .body(request)
                    .when().post("/api/table/time-based")
                    .then().statusCode(201)
                    .extract().as(TimeBasedTableResponse.class);

            assertAll(
                    () -> assertThat(response.info().name()).isEqualTo(request.info().name()),
                    () -> assertThat(response.table()).hasSize(request.table().size())
            );
        }
    }

    @Nested
    class GetTable {

        @Test
        void 시간총량제_테이블을_조회한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            TimeBasedTable bitoTable = timeBasedTableGenerator.generate(bito);
            timeBasedTimeBoxGenerator.generate(bitoTable, 1);
            timeBasedTimeBoxGenerator.generate(bitoTable, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            TimeBasedTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().get("/api/table/time-based/{tableId}")
                    .then().statusCode(200)
                    .extract().as(TimeBasedTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.table()).hasSize(2)
            );
        }
    }

    @Nested
    class UpdateTable {

        @Test
        void 시간총량제_토론_테이블을_업데이트한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            TimeBasedTable bitoTable = timeBasedTableGenerator.generate(bito);
            TimeBasedTableCreateRequest renewTableRequest = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("비토 테이블", "주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            TimeBasedTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .body(renewTableRequest)
                    .when().put("/api/table/time-based/{tableId}")
                    .then().statusCode(200)
                    .extract().as(TimeBasedTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.info().name()).isEqualTo(renewTableRequest.info().name()),
                    () -> assertThat(response.table()).hasSize(renewTableRequest.table().size())
            );
        }
    }

    @Nested
    class DoDebate {

        @Test
        void 시간총량제_토론을_시작한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            TimeBasedTable bitoTable = timeBasedTableGenerator.generate(bito);
            timeBasedTimeBoxGenerator.generate(bitoTable, 1);
            timeBasedTimeBoxGenerator.generate(bitoTable, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            TimeBasedTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().patch("/api/table/time-based/{tableId}/debate")
                    .then().statusCode(200)
                    .extract().as(TimeBasedTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.info().name()).isEqualTo(bitoTable.getName()),
                    () -> assertThat(response.table()).hasSize(2)
            );
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 시간총량제_토론_테이블을_삭제한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            TimeBasedTable bitoTable = timeBasedTableGenerator.generate(bito);
            timeBasedTimeBoxGenerator.generate(bitoTable, 1);
            timeBasedTimeBoxGenerator.generate(bitoTable, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().delete("/api/table/time-based/{tableId}")
                    .then().statusCode(204);
        }
    }
}
