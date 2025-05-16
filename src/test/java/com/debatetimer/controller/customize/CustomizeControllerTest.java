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
import com.debatetimer.exception.ErrorResponse;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.NullAndEmptyAndBlankSource;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.http.HttpStatus;

class CustomizeControllerTest extends BaseControllerTest {

    @Nested
    class Save {

        @Test
        void 사용자_지정_테이블을_생성한다() {
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, null)
                    )
            );

            CustomizeTableResponse response = sendCustomizeTableCreateRequest(request, HttpStatus.CREATED)
                    .extract().as(CustomizeTableResponse.class);

            assertAll(
                    () -> assertThat(response.info().name()).isEqualTo(request.info().name()),
                    () -> assertThat(response.table()).hasSize(request.table().size())
            );
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_생성할때_테이블_이름은_개행문자_외_다른_글자가_포함되야한다(String tableName) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("name", tableName);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 사용자_지정_테이블을_생성할때_테이블_주제는_null이_올_수_없다(String agenda) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("agenda", agenda);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_생성할때_찬성팀_이름은_개행문자_외_다른_글자가_포함되야한다(String prosTeamName) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("prosTeamName", prosTeamName);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_생성할때_반대팀_이름은_개행문자_외_다른_글자가_포함되야한다(String consTeamName) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("consTeamName", consTeamName);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 사용자_지정_테이블을_생성할때_타임박스_입장은_null이_올_수_없다(Stance stance) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("stance", stance);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_생성할때_타임박스_발언_유형은_개행문자_외_다른_글자가_포함되야한다(String speechType) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("speechType", speechType);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 사용자_지정_테이블을_생성할때_타임박스_타입은_null이_올_수_없다(CustomizeBoxType boxType) {
            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("boxType", boxType);

            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        private CustomizeTableCreateRequest createCustomizeTableCreateRequest(String field, Object value) {
            String name = field.equals("name") ? (String) value : "자유 테이블";
            String agenda = field.equals("agenda") ? (String) value : "주제";
            String prosTeamName = field.equals("prosTeamName") ? (String) value : "찬성";
            String consTeamName = field.equals("consTeamName") ? (String) value : "반대";

            Stance stance = field.equals("stance") ? (Stance) value : Stance.PROS;
            String speechType = field.equals("speechType") ? (String) value : "입론1";
            CustomizeBoxType boxType = field.equals("boxType") ? (CustomizeBoxType) value : CustomizeBoxType.NORMAL;

            return new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest(name, agenda, prosTeamName,
                            consTeamName, true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(stance, speechType, boxType,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, null)
                    )
            );
        }

        private ValidatableResponse sendCustomizeTableCreateRequest(
                CustomizeTableCreateRequest request,
                HttpStatus statusCode
        ) {
            Member bito = memberGenerator.generate("default@gmail.com");
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);
            return given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .body(request)
                    .when().post("/api/table/customize")
                    .then().statusCode(statusCode.value());
        }
    }

    @Nested
    class GetTable {

        @Test
        void 사용자_지정_테이블을_조회한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generateNotExistSpeaker(bitoTable, CustomizeBoxType.NORMAL, 2);
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
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);
            CustomizeTableCreateRequest renewTableRequest = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, null)
                    )
            );

            CustomizeTableResponse response = sendCustomizeTableCreateRequest(
                    renewTableRequest, HttpStatus.OK, bitoTable, headers
            ).extract().as(CustomizeTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.info().name()).isEqualTo(renewTableRequest.info().name()),
                    () -> assertThat(response.table()).hasSize(renewTableRequest.table().size())
            );
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_업데이트할때_테이블_이름은_개행문자_외_다른_글자가_포함되야한다(String tableName) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("name", tableName);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 사용자_지정_테이블을_업데이트할때_테이블_주제는_null이_올_수_없다(String agenda) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("agenda", agenda);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_업데이트할때_찬성팀_이름은_개행문자_외_다른_글자가_포함되야한다(String prosTeamName) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("prosTeamName", prosTeamName);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_업데이트할때_반대팀_이름은_개행문자_외_다른_글자가_포함되야한다(String consTeamName) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("consTeamName", consTeamName);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 사용자_지정_테이블을_업데이트할때_타임박스_입장은_null이_올_수_없다(Stance stance) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("stance", stance);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 사용자_지정_테이블을_업데이트할때_타임박스_발언_유형은_개행문자_외_다른_글자가_포함되야한다(String speechType) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("speechType", speechType);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @NullSource
        @ParameterizedTest
        void 사용자_지정_테이블을_업데이트할때_타임박스_타입은_null이_올_수_없다(CustomizeBoxType boxType) {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            CustomizeTableCreateRequest request = createCustomizeTableCreateRequest("boxType", boxType);
            ErrorResponse errorResponse = sendCustomizeTableCreateRequest(
                    request, HttpStatus.BAD_REQUEST, bitoTable, headers
            ).extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        private CustomizeTableCreateRequest createCustomizeTableCreateRequest(String field, Object value) {
            String name = field.equals("name") ? (String) value : "자유 테이블";
            String agenda = field.equals("agenda") ? (String) value : "주제";
            String prosTeamName = field.equals("prosTeamName") ? (String) value : "찬성";
            String consTeamName = field.equals("consTeamName") ? (String) value : "반대";

            Stance stance = field.equals("stance") ? (Stance) value : Stance.PROS;
            String speechType = field.equals("speechType") ? (String) value : "입론1";
            CustomizeBoxType boxType = field.equals("boxType") ? (CustomizeBoxType) value : CustomizeBoxType.NORMAL;

            return new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest(name, agenda, prosTeamName,
                            consTeamName, true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(stance, speechType, boxType,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, null)
                    )
            );
        }

        private ValidatableResponse sendCustomizeTableCreateRequest(
                CustomizeTableCreateRequest request,
                HttpStatus statusCode,
                CustomizeTable table,
                Headers headers
        ) {
            return given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", table.getId())
                    .headers(headers)
                    .body(request)
                    .when().put("/api/table/customize/{tableId}")
                    .then().statusCode(statusCode.value());
        }
    }

    @Nested
    class Debate {

        @Test
        void 사용자_지정_토론을_시작한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generateNotExistSpeaker(bitoTable, CustomizeBoxType.NORMAL, 2);
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
            customizeTimeBoxGenerator.generateNotExistSpeaker(bitoTable, CustomizeBoxType.NORMAL, 2);
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
