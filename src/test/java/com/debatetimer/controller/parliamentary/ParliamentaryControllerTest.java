package com.debatetimer.controller.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParliamentaryControllerTest extends BaseControllerTest {

    @Nested
    class Save {

        @Test
        void 의회식_테이블을_생성한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new ParliamentaryTableInfoCreateRequest("비토 테이블", "주제", true, true),
                    List.of(
                            new ParliamentaryTimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new ParliamentaryTimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)
                    )
            );
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            ParliamentaryTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .body(request)
                    .when().post("/api/table/parliamentary")
                    .then().statusCode(201)
                    .extract().as(ParliamentaryTableResponse.class);

            assertAll(
                    () -> assertThat(response.info().name()).isEqualTo(request.info().name()),
                    () -> assertThat(response.table()).hasSize(request.table().size())
            );
        }
    }

    @Nested
    class GetTable {

        @Test
        void 의회식_테이블을_조회한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable bitoTable = parliamentaryTableGenerator.generate(bito);
            parliamentaryTimeBoxGenerator.generate(bitoTable, 1);
            parliamentaryTimeBoxGenerator.generate(bitoTable, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            ParliamentaryTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().get("/api/table/parliamentary/{tableId}")
                    .then().statusCode(200)
                    .extract().as(ParliamentaryTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.table()).hasSize(2)
            );
        }
    }

    @Nested
    class UpdateTable {

        @Test
        void 의회식_토론_테이블을_업데이트한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable bitoTable = parliamentaryTableGenerator.generate(bito);
            ParliamentaryTableCreateRequest renewTableRequest = new ParliamentaryTableCreateRequest(
                    new ParliamentaryTableInfoCreateRequest("비토 테이블", "주제", true, true),
                    List.of(
                            new ParliamentaryTimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new ParliamentaryTimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)
                    )
            );
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            ParliamentaryTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .body(renewTableRequest)
                    .when().put("/api/table/parliamentary/{tableId}")
                    .then().statusCode(200)
                    .extract().as(ParliamentaryTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.info().name()).isEqualTo(renewTableRequest.info().name()),
                    () -> assertThat(response.table()).hasSize(renewTableRequest.table().size())
            );
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 의회식_토론_테이블을_삭제한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable bitoTable = parliamentaryTableGenerator.generate(bito);
            parliamentaryTimeBoxGenerator.generate(bitoTable, 1);
            parliamentaryTimeBoxGenerator.generate(bitoTable, 2);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .headers(headers)
                    .when().delete("/api/table/parliamentary/{tableId}")
                    .then().statusCode(204);
        }
    }
}
