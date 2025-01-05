package com.debatetimer.controller.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.BaseControllerTest;
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
import org.springframework.restdocs.restassured.RestDocumentationFilter;

class ParliamentaryControllerTest extends BaseControllerTest {

    @Nested
    class save {

        private final RestDocumentationFilter document = document("table/create")
                .description("새로운 의회식 토론 시간표 생성")
                .queryParameter(
                        parameterWithName("memberId").description("멤버 ID")
                )
                .requestBodyField(
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                )
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                )
                .build();

        @Test
        void 토론_테이블을_생성한다() {
            Member bito = fixtureGenerator.generateMember("비토");
            ParliamentaryTableCreateRequest bitoTableRequest = dtoGenerator.generateParliamentaryTableCreateRequest(
                    "비토 테이블");
            TableInfoCreateRequest requestTableInfo = bitoTableRequest.info();
            List<TimeBoxCreateRequest> requestTimeBoxes = bitoTableRequest.table();

            ParliamentaryTableResponse response = given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", bito.getId())
                    .body(bitoTableRequest)
                    .when().post("/api/table/parliamentary")
                    .then().statusCode(201)
                    .extract().as(ParliamentaryTableResponse.class);

            assertAll(
                    () -> assertThat(response.info().name()).isEqualTo(requestTableInfo.name()),
                    () -> assertThat(response.table()).hasSize(requestTimeBoxes.size())
            );
        }
    }

    @Nested
    class getTable {

        private final RestDocumentationFilter document = document("table/get")
                .description("의회식 토론 시간표 조회")
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                )
                .queryParameter(
                        parameterWithName("memberId").description("멤버 ID")
                )
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                )
                .build();

        @Test
        void 의회식_테이블을_조회한다() {
            Member bito = fixtureGenerator.generateMember("비토");
            ParliamentaryTable bitoTable = fixtureGenerator.generateParliamentaryTable(bito);
            fixtureGenerator.generateParliamentaryTimeBox(bitoTable, 1);
            fixtureGenerator.generateParliamentaryTimeBox(bitoTable, 2);

            ParliamentaryTableResponse response = given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", bito.getId())
                    .pathParam("tableId", bitoTable.getId())
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
    class updateTable {

        private final RestDocumentationFilter document = document("table/put")
                .description("의회식 토론 시간표 수정")
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                )
                .queryParameter(
                        parameterWithName("memberId").description("멤버 ID")
                )
                .requestBodyField(
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                )
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                )
                .build();

        @Test
        void 의회식_토론_테이블을_업데이트한다() {
            Member bito = fixtureGenerator.generateMember("커찬");
            ParliamentaryTable bitoTable = fixtureGenerator.generateParliamentaryTable(bito);
            ParliamentaryTableCreateRequest renewTableRequest = dtoGenerator.generateParliamentaryTableCreateRequest(
                    "새로운 테이블");
            TableInfoCreateRequest renewTableInfo = renewTableRequest.info();
            List<TimeBoxCreateRequest> renewTimeBoxes = renewTableRequest.table();

            ParliamentaryTableResponse response = given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", bito.getId())
                    .pathParam("tableId", bitoTable.getId())
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
    class deleteTable {

        private final RestDocumentationFilter document = document("table/delete")
                .description("의회식 토론 시간표 삭제")
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                )
                .queryParameter(
                        parameterWithName("memberId").description("멤버 ID")
                )
                .build();

        @Test
        void 의회식_토론_테이블을_삭제한다() {
            Member bito = fixtureGenerator.generateMember("비토");
            ParliamentaryTable bitoTable = fixtureGenerator.generateParliamentaryTable(bito);
            fixtureGenerator.generateParliamentaryTimeBox(bitoTable, 1);
            fixtureGenerator.generateParliamentaryTimeBox(bitoTable, 2);

            given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", bito.getId())
                    .pathParam("tableId", bitoTable.getId())
                    .when().delete("/api/table/parliamentary/{tableId}")
                    .then().statusCode(204);
        }
    }
}
