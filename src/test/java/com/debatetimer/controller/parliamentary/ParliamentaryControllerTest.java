package com.debatetimer.controller.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParliamentaryControllerTest extends BaseControllerTest {

    @Nested
    class Save {

        @Test
        void 의회식_테이블을_생성한다() {
            Member bito = memberGenerator.generate("비토");
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new TableInfoCreateRequest("비토 테이블", "주제"),
                    List.of(
                            new TimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 3, 1),
                            new TimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 3, 1)
                    )
            );

            ParliamentaryTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", bito.getId())
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
            Member bito = memberGenerator.generate("비토");
            ParliamentaryTable bitoTable = tableGenerator.generate(bito);
            timeBoxGenerator.generate(bitoTable, 1);
            timeBoxGenerator.generate(bitoTable, 2);

            ParliamentaryTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .queryParam("memberId", bito.getId())
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
            Member bito = memberGenerator.generate("비토");
            ParliamentaryTable bitoTable = tableGenerator.generate(bito);
            TableInfoCreateRequest renewTableInfo = new TableInfoCreateRequest("비토 테이블", "주제");
            List<TimeBoxCreateRequest> renewTimeBoxes = List.of(
                    new TimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 3, 1),
                    new TimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 3, 1)
            );
            ParliamentaryTableCreateRequest renewTableRequest = new ParliamentaryTableCreateRequest(
                    renewTableInfo,
                    renewTimeBoxes
            );

            ParliamentaryTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .queryParam("memberId", bito.getId())
                    .body(renewTableRequest)
                    .when().put("/api/table/parliamentary/{tableId}")
                    .then().statusCode(200)
                    .extract().as(ParliamentaryTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.info().name()).isEqualTo(renewTableInfo.name()),
                    () -> assertThat(response.table()).hasSize(renewTimeBoxes.size())
            );
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 의회식_토론_테이블을_삭제한다() {
            Member bito = memberGenerator.generate("비토");
            ParliamentaryTable bitoTable = tableGenerator.generate(bito);
            timeBoxGenerator.generate(bitoTable, 1);
            timeBoxGenerator.generate(bitoTable, 2);

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .queryParam("memberId", bito.getId())
                    .when().delete("/api/table/parliamentary/{tableId}")
                    .then().statusCode(204);
        }
    }
}
