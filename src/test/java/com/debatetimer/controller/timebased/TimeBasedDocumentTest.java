package com.debatetimer.controller.timebased;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.dto.timebased.request.TimeBasedTableCreateRequest;
import com.debatetimer.dto.timebased.request.TimeBasedTableInfoCreateRequest;
import com.debatetimer.dto.timebased.request.TimeBasedTimeBoxCreateRequest;
import com.debatetimer.dto.timebased.response.TimeBasedTableInfoResponse;
import com.debatetimer.dto.timebased.response.TimeBasedTableResponse;
import com.debatetimer.dto.timebased.response.TimeBasedTimeBoxResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;

public class TimeBasedDocumentTest extends BaseDocumentTest {

    @Nested
    class Save {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.TIME_BASED_API)
                .summary("새로운 시간총량제 토론 시간표 생성")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .requestBodyField(
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.type").type(STRING).description("토론 형식"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 시간총량제_테이블_생성_성공() {
            TimeBasedTableCreateRequest request = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("비토 테이블 1", "토론 주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));
            TimeBasedTableResponse response = new TimeBasedTableResponse(
                    5L,
                    new TimeBasedTableInfoResponse("비토 테이블 1", TableType.PARLIAMENTARY, "토론 주제", true, true),
                    List.of(
                            new TimeBasedTimeBoxResponse(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null, 1),
                            new TimeBasedTimeBoxResponse(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180, 60, 1)
                    )
            );
            doReturn(response).when(timeBasedService).save(eq(request), any());

            var document = document("time_based/post", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .body(request)
                    .when().post("/api/table/time-based")
                    .then().statusCode(201);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "INVALID_TABLE_NAME_LENGTH",
                        "INVALID_TABLE_NAME_FORM",
                        "INVALID_TABLE_TIME",
                        "INVALID_TIME_BOX_SEQUENCE",
                        "INVALID_TIME_BOX_SPEAKER",
                        "INVALID_TIME_BOX_TIME",
                        "INVALID_TIME_BOX_STANCE",
                        "INVALID_TIME_BOX_FORMAT"
                }
        )
        @ParameterizedTest
        void 시간총량제_테이블_생성_실패(ClientErrorCode errorCode) {
            TimeBasedTableCreateRequest request = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("비토 테이블 1", "토론 주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));
            doThrow(new DTClientErrorException(errorCode)).when(timeBasedService).save(eq(request), any());

            var document = document("time_based/post", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .body(request)
                    .when().post("/api/table/time-based")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class GetTable {

        private final RestDocumentationRequest requestDocument = request()
                .summary("시간총량제 토론 시간표 조회")
                .tag(Tag.TIME_BASED_API)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.type").type(STRING).description("토론 형식"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 시간총량제_테이블_조회_성공() {
            long tableId = 5L;
            TimeBasedTableResponse response = new TimeBasedTableResponse(
                    5L,
                    new TimeBasedTableInfoResponse("비토 테이블 1", TableType.PARLIAMENTARY, "토론 주제", true, true),
                    List.of(
                            new TimeBasedTimeBoxResponse(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null, 1),
                            new TimeBasedTimeBoxResponse(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180, 60, 1)
                    )
            );
            doReturn(response).when(timeBasedService).findTable(eq(tableId), any());

            var document = document("time_based/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().get("/api/table/time-based/{tableId}")
                    .then().statusCode(200);
        }

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND", "NOT_TABLE_OWNER"})
        void 시간총량제_테이블_조회_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(timeBasedService).findTable(eq(tableId), any());

            var document = document("time_based/get", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().get("/api/table/time-based/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class UpdateTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.TIME_BASED_API)
                .summary("시간총량제 토론 시간표 수정")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                )
                .requestBodyField(
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.type").type(STRING).description("토론 형식"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 시간총량제_토론_테이블_수정() {
            long tableId = 5L;
            TimeBasedTableCreateRequest request = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("비토 테이블 2", "토론 주제 2", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));
            TimeBasedTableResponse response = new TimeBasedTableResponse(
                    5L,
                    new TimeBasedTableInfoResponse("비토 테이블 2", TableType.PARLIAMENTARY, "토론 주제 2", true, true),
                    List.of(
                            new TimeBasedTimeBoxResponse(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null, 1),
                            new TimeBasedTimeBoxResponse(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180, 60, 1)
                    )
            );
            doReturn(response).when(timeBasedService).updateTable(eq(request), eq(tableId), any());

            var document = document("time_based/patch", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .body(request)
                    .when().put("/api/table/time-based/{tableId}")
                    .then().statusCode(200);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "INVALID_TABLE_NAME_LENGTH",
                        "INVALID_TABLE_NAME_FORM",
                        "INVALID_TABLE_TIME",
                        "INVALID_TIME_BOX_SEQUENCE",
                        "INVALID_TIME_BOX_SPEAKER",
                        "INVALID_TIME_BOX_TIME",
                        "INVALID_TIME_BOX_STANCE",
                        "INVALID_TIME_BOX_FORMAT"
                }
        )
        @ParameterizedTest
        void 시간총량제_테이블_생성_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            TimeBasedTableCreateRequest request = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("비토 테이블 2", "토론 주제 2", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));
            doThrow(new DTClientErrorException(errorCode)).when(timeBasedService)
                    .updateTable(eq(request), eq(tableId), any());

            var document = document("time_based/patch", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .body(request)
                    .when().put("/api/table/time-based/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class Debate {

        private final RestDocumentationRequest requestDocument = request()
                .summary("시간총량제 토론 시작")
                .tag(Tag.TIME_BASED_API)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.type").type(STRING).description("토론 형식"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 시간총량제_토론_진행_성공() {
            long tableId = 5L;
            TimeBasedTableResponse response = new TimeBasedTableResponse(
                    5L,
                    new TimeBasedTableInfoResponse("비토 테이블 1", TableType.PARLIAMENTARY, "토론 주제", true, true),
                    List.of(
                            new TimeBasedTimeBoxResponse(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null, 1),
                            new TimeBasedTimeBoxResponse(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180, 60, 1)
                    )
            );
            doReturn(response).when(timeBasedService).updateUsedAt(eq(tableId), any());

            var document = document("time_based/patch_debate", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().patch("/api/table/time-based/{tableId}/debate")
                    .then().statusCode(200);
        }

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND", "NOT_TABLE_OWNER"})
        void 시간총량제_토론_진행_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(timeBasedService).updateUsedAt(eq(tableId), any());

            var document = document("time_based/get", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().patch("/api/table/time-based/{tableId}/debate")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class DeleteTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.TIME_BASED_API)
                .summary("시간총량제 토론 시간표 삭제")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                );

        @Test
        void 시간총량제_테이블_삭제_성공() {
            long tableId = 5L;
            doNothing().when(timeBasedService).deleteTable(eq(tableId), any());

            var document = document("time_based/delete", 204)
                    .request(requestDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().delete("/api/table/time-based/{tableId}")
                    .then().statusCode(204);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND", "NOT_TABLE_OWNER"})
        @ParameterizedTest
        void 시간총량제_테이블_삭제_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(timeBasedService).deleteTable(eq(tableId), any());

            var document = document("time_based/delete", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().delete("/api/table/time-based/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
