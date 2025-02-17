package com.debatetimer.controller.parliamentary;

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
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.dto.parliamentary.response.TableInfoResponse;
import com.debatetimer.dto.parliamentary.response.TimeBoxResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;

public class ParliamentaryDocumentTest extends BaseDocumentTest {

    @Nested
    class Save {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.PARLIAMENTARY_API)
                .summary("새로운 의회식 토론 시간표 생성")
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
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 의회식_테이블_생성_성공() {
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new TableInfoCreateRequest("비토 테이블 1", "토론 주제", true, true),
                    List.of(
                            new TimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new TimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)
                    )
            );
            ParliamentaryTableResponse response = new ParliamentaryTableResponse(
                    5L,
                    new TableInfoResponse("비토 테이블 1", TableType.PARLIAMENTARY, "토론 주제", true, true),
                    List.of(
                            new TimeBoxResponse(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new TimeBoxResponse(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)
                    )
            );
            doReturn(response).when(parliamentaryService).save(eq(request), any());

            var document = document("parliamentary/post", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .body(request)
                    .when().post("/api/table/parliamentary")
                    .then().statusCode(201);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "INVALID_TABLE_NAME_LENGTH",
                        "INVALID_TABLE_NAME_FORM",
                        "INVALID_TABLE_TIME",
                        "INVALID_TIME_BOX_TIME",
                        "INVALID_TIME_BOX_STANCE"
                }
        )
        @ParameterizedTest
        void 의회식_테이블_생성_실패(ClientErrorCode errorCode) {
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new TableInfoCreateRequest("비토 테이블 1", "토론 주제", true, true),
                    List.of(
                            new TimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new TimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)
                    )
            );
            doThrow(new DTClientErrorException(errorCode)).when(parliamentaryService).save(eq(request), any());

            var document = document("parliamentary/post", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .body(request)
                    .when().post("/api/table/parliamentary")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class GetTable {

        private final RestDocumentationRequest requestDocument = request()
                .summary("의회식 토론 시간표 조회")
                .tag(Tag.PARLIAMENTARY_API)
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
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 의회식_테이블_조회_성공() {
            long memberId = 4L;
            long tableId = 5L;
            ParliamentaryTableResponse response = new ParliamentaryTableResponse(
                    5L,
                    new TableInfoResponse("비토 테이블 1", TableType.PARLIAMENTARY, "토론 주제", true, true),
                    List.of(
                            new TimeBoxResponse(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new TimeBoxResponse(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)
                    )
            );
            doReturn(response).when(parliamentaryService).findTable(eq(tableId), any());

            var document = document("parliamentary/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().get("/api/table/parliamentary/{tableId}")
                    .then().statusCode(200);
        }

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND", "NOT_TABLE_OWNER"})
        void 의회식_테이블_조회_실패(ClientErrorCode errorCode) {
            long memberId = 4L;
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(parliamentaryService).findTable(eq(tableId), any());

            var document = document("parliamentary/get", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().get("/api/table/parliamentary/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class UpdateTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.PARLIAMENTARY_API)
                .summary("의회식 토론 시간표 수정")
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
                        fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                );

        @Test
        void 의회식_토론_테이블_수정() {
            long memberId = 4L;
            long tableId = 5L;
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new TableInfoCreateRequest("비토 테이블 2", "토론 주제 2", true, true),
                    List.of(
                            new TimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 300, 1),
                            new TimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 300, 1)
                    )
            );
            ParliamentaryTableResponse response = new ParliamentaryTableResponse(
                    5L,
                    new TableInfoResponse("비토 테이블 2", TableType.PARLIAMENTARY, "토론 주제 2", true, true),
                    List.of(
                            new TimeBoxResponse(Stance.PROS, ParliamentaryBoxType.OPENING, 300, 1),
                            new TimeBoxResponse(Stance.CONS, ParliamentaryBoxType.OPENING, 300, 1)
                    )
            );
            doReturn(response).when(parliamentaryService).updateTable(eq(request), eq(tableId), any());

            var document = document("parliamentary/put", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .body(request)
                    .when().put("/api/table/parliamentary/{tableId}")
                    .then().statusCode(200);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "INVALID_TABLE_NAME_LENGTH",
                        "INVALID_TABLE_NAME_FORM",
                        "INVALID_TABLE_TIME",
                        "INVALID_TIME_BOX_TIME",
                        "INVALID_TIME_BOX_STANCE",
                        "NOT_TABLE_OWNER"
                }
        )
        @ParameterizedTest
        void 의회식_테이블_생성_실패(ClientErrorCode errorCode) {
            long memberId = 4L;
            long tableId = 5L;
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new TableInfoCreateRequest("비토 테이블 2", "토론 주제 2", true, true),
                    List.of(
                            new TimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 300, 1),
                            new TimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 300, 1)
                    )
            );
            doThrow(new DTClientErrorException(errorCode)).when(parliamentaryService)
                    .updateTable(eq(request), eq(tableId), any());

            var document = document("parliamentary/put", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .body(request)
                    .when().put("/api/table/parliamentary/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class DeleteTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.PARLIAMENTARY_API)
                .summary("의회식 토론 시간표 삭제")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                );

        @Test
        void 의회식_테이블_삭제_성공() {
            long memberId = 4L;
            long tableId = 5L;
            doNothing().when(parliamentaryService).deleteTable(eq(tableId), any());

            var document = document("parliamentary/delete", 204)
                    .request(requestDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().delete("/api/table/parliamentary/{tableId}")
                    .then().statusCode(204);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND", "NOT_TABLE_OWNER"})
        @ParameterizedTest
        void 의회식_테이블_삭제_실패(ClientErrorCode errorCode) {
            long memberId = 4L;
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(parliamentaryService).deleteTable(eq(tableId), any());

            var document = document("parliamentary/delete", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().delete("/api/table/parliamentary/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
