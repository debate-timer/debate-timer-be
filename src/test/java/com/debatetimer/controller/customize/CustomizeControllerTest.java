package com.debatetimer.controller.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTableInfoCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomizeControllerTest extends BaseControllerTest {

    @Nested
    class Save {

        @Test
        void 사용자_지정_테이블을_생성한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .body(request)
                    .when().post("/api/table/customize")
                    .then().statusCode(201)
                    .extract().as(CustomizeTableResponse.class);

            assertAll(
                    () -> assertThat(response.info().name()).isEqualTo(request.info().name()),
                    () -> assertThat(response.table()).hasSize(request.table().size())
            );
        }
    }

    @Nested
    class GetTable {

        @Test
        void 사용자_지정_테이블을_조회한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().get("/api/table/customize/{tableId}")
                    .then().statusCode(200)
                    .extract().as(CustomizeTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.table()).hasSize(2)
            );
        }
    }

    @Nested
    class UpdateTable {

        @Test
        void 사용자_지정_토론_테이블을_업데이트한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            CustomizeTableCreateRequest renewTableRequest = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .body(renewTableRequest)
                    .when().put("/api/table/customize/{tableId}")
                    .then().statusCode(200)
                    .extract().as(CustomizeTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.info().name()).isEqualTo(renewTableRequest.info().name()),
                    () -> assertThat(response.table()).hasSize(renewTableRequest.table().size())
            );
        }
    }

    @Nested
    class Debate {

        @Test
        void 사용자_지정_토론을_시작한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().patch("/api/table/customize/{tableId}/debate")
                    .then().statusCode(200)
                    .extract().as(CustomizeTableResponse.class);

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
        void 사용자_지정_토론_테이블을_삭제한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().delete("/api/table/customize/{tableId}")
                    .then().statusCode(204);
        }
    }
}
