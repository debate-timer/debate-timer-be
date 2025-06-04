package com.debatetimer.controller.customize;

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
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTableInfoCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableInfoResponse;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.dto.customize.response.CustomizeTimeBoxResponse;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;

public class CustomizeDocumentTest extends BaseDocumentTest {

    @Nested
    class Save {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.CUSTOMIZE_API)
                .summary("새로운 사용자 지정 토론 시간표 생성")
                .description("""
                        ### 타임 박스 종류에 따른 요청 값
                        | 타임 박스 종류 | 필수 입력 | 선택 입력 | null 입력 |
                        | :---: | ---| --- | --- |
                        | 커스텀 타임 박스 | stance, speechType, boxType, time | speaker | timePerTeam, timePerSpeaking |
                        | 자유 토론 타임 박스 | stance, speechType, boxType, timePerTeam | speaker, timePerSpeaking | time |
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .requestBodyField(
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.prosTeamName").type(STRING).description("찬성팀 팀명"),
                        fieldWithPath("info.consTeamName").type(STRING).description("반대팀 팀명"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].boxType").type(STRING).description("타임 박스 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)").optional(),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.type").type(STRING).description("토론 테이블 유형"),
                        fieldWithPath("info.prosTeamName").type(STRING).description("찬성팀 팀명"),
                        fieldWithPath("info.consTeamName").type(STRING).description("반대팀 팀명"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].boxType").type(STRING).description("타임 박스 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)").optional(),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );

        @Test
        void 사용자_지정_테이블_생성_성공() {
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxCreateRequest(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            CustomizeTableResponse response = new CustomizeTableResponse(
                    5L,
                    new CustomizeTableInfoResponse("나의 테이블", TableType.CUSTOMIZE, "토론 주제",
                            "찬성", "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxResponse(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxResponse(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            doReturn(response).when(customizeService).save(eq(request), any());

            var document = document("customize/post", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .body(request)
                    .when().post("/api/table/customize")
                    .then().statusCode(201);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "INVALID_TABLE_NAME_LENGTH",
                        "INVALID_TABLE_NAME_FORM",
                        "INVALID_TABLE_TIME",
                        "INVALID_AGENDA_LENGTH",
                        "INVALID_TIME_BOX_SEQUENCE",
                        "INVALID_TIME_BOX_TIME",
                        "INVALID_TIME_BOX_STANCE",
                        "INVALID_TIME_BOX_FORMAT"
                }
        )
        @ParameterizedTest
        void 사용자_지정_테이블_생성_실패(ClientErrorCode errorCode) {
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxCreateRequest(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            doThrow(new DTClientErrorException(errorCode)).when(customizeService).save(eq(request), any());

            var document = document("customize/post", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .body(request)
                    .when().post("/api/table/customize")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class GetTable {

        private final RestDocumentationRequest requestDocument = request()
                .summary("사용자_지정 토론 시간표 조회")
                .description("""
                        ### 타임 박스 종류에 따른 웅답 값
                        | 타임 박스 종류 | 필수 입력 | 선택 입력 | null 입력 |
                        | :---: | ---| --- | --- |
                        | 커스텀 타임 박스 | stance, speechType, boxType, time | speaker | timePerTeam, timePerSpeaking |
                        | 자유 토론 타임 박스 | stance, speechType, boxType, timePerTeam | speaker, timePerSpeaking | time |
                        """)
                .tag(Tag.CUSTOMIZE_API)
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
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.type").type(STRING).description("토론 테이블 유형"),
                        fieldWithPath("info.prosTeamName").type(STRING).description("찬성팀 팀명"),
                        fieldWithPath("info.consTeamName").type(STRING).description("반대팀 팀명"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].boxType").type(STRING).description("타임 박스 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)").optional(),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );

        @Test
        void 사용자_지정_테이블_조회_성공() {
            long tableId = 5L;
            CustomizeTableResponse response = new CustomizeTableResponse(
                    5L,
                    new CustomizeTableInfoResponse("나의 테이블", TableType.CUSTOMIZE, "토론 주제",
                            "찬성", "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxResponse(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxResponse(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            doReturn(response).when(customizeService).findTable(eq(tableId), any());

            var document = document("customize/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().get("/api/table/customize/{tableId}")
                    .then().statusCode(200);
        }

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND"})
        void 사용자_지정_테이블_조회_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(customizeService).findTable(eq(tableId), any());

            var document = document("customize/get", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().get("/api/table/customize/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class UpdateTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.CUSTOMIZE_API)
                .summary("사용자 지정 토론 시간표 수정")
                .description("""
                        ### 타임 박스 종류에 따른 요청/웅답 값
                        | 타임 박스 종류 | 필수 입력 | 선택 입력 | null 입력 |
                        | :---: | ---| --- | --- |
                        | 커스텀 타임 박스 | stance, speechType, boxType, time | speaker | timePerTeam, timePerSpeaking |
                        | 자유 토론 타임 박스 | stance, speechType, boxType, timePerTeam | speaker, timePerSpeaking | time |
                        """)
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
                        fieldWithPath("info.prosTeamName").type(STRING).description("찬성팀 팀명"),
                        fieldWithPath("info.consTeamName").type(STRING).description("반대팀 팀명"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].boxType").type(STRING).description("타임 박스 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)").optional(),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("테이블 ID"),
                        fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                        fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.type").type(STRING).description("토론 테이블 유형"),
                        fieldWithPath("info.prosTeamName").type(STRING).description("찬성팀 팀명"),
                        fieldWithPath("info.consTeamName").type(STRING).description("반대팀 팀명"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].boxType").type(STRING).description("타임 박스 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)").optional(),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );

        @Test
        void 사용자_지정_토론_테이블_수정() {
            long tableId = 5L;
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxCreateRequest(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            CustomizeTableResponse response = new CustomizeTableResponse(
                    5L,
                    new CustomizeTableInfoResponse("나의 테이블", TableType.CUSTOMIZE, "주제",
                            "찬성", "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxResponse(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxResponse(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            doReturn(response).when(customizeService).updateTable(eq(request), eq(tableId), any());

            var document = document("customize/patch", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .body(request)
                    .when().put("/api/table/customize/{tableId}")
                    .then().statusCode(200);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "TABLE_NOT_FOUND",
                        "INVALID_TABLE_NAME_LENGTH",
                        "INVALID_TABLE_NAME_FORM",
                        "INVALID_TABLE_TIME",
                        "INVALID_AGENDA_LENGTH",
                        "INVALID_TIME_BOX_SEQUENCE",
                        "INVALID_TIME_BOX_TIME",
                        "INVALID_TIME_BOX_STANCE",
                        "INVALID_TIME_BOX_FORMAT"
                }
        )
        @ParameterizedTest
        void 사용자_지정_테이블_생성_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxCreateRequest(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxCreateRequest(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            doThrow(new DTClientErrorException(errorCode)).when(customizeService)
                    .updateTable(eq(request), eq(tableId), any());

            var document = document("customize/patch", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .body(request)
                    .when().put("/api/table/customize/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class Debate {

        private final RestDocumentationRequest requestDocument = request()
                .summary("사용자 지정 토론 시작")
                .description("""
                        ### 타임 박스 종류에 따른 웅답 값
                        | 타임 박스 종류 | 필수 입력 | 선택 입력 | null 입력 |
                        | :---: | ---| --- | --- |
                        | 커스텀 타임 박스 | stance, speechType, boxType, time | speaker | timePerTeam, timePerSpeaking |
                        | 자유 토론 타임 박스 | stance, speechType, boxType, timePerTeam | speaker, timePerSpeaking | time |
                        """)
                .tag(Tag.CUSTOMIZE_API)
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
                        fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                        fieldWithPath("info.type").type(STRING).description("토론 테이블 유형"),
                        fieldWithPath("info.prosTeamName").type(STRING).description("찬성팀 팀명"),
                        fieldWithPath("info.consTeamName").type(STRING).description("반대팀 팀명"),
                        fieldWithPath("info.warningBell").type(BOOLEAN).description("30초 종소리 유무"),
                        fieldWithPath("info.finishBell").type(BOOLEAN).description("발언 종료 종소리 유무"),
                        fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                        fieldWithPath("table[].stance").type(STRING).description("입장"),
                        fieldWithPath("table[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("table[].boxType").type(STRING).description("타임 박스 유형"),
                        fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)").optional(),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );


        @Test
        void 사용자_지정_토론_진행_성공() {
            long tableId = 5L;
            CustomizeTableResponse response = new CustomizeTableResponse(
                    5L,
                    new CustomizeTableInfoResponse("나의 테이블", TableType.CUSTOMIZE, "주제",
                            "찬성", "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxResponse(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "콜리"),
                            new CustomizeTimeBoxResponse(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, null, null, "비토"),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, 120, null),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, 360, null, null)
                    )
            );
            doReturn(response).when(customizeService).updateUsedAt(eq(tableId), any());

            var document = document("customize/patch_debate", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().patch("/api/table/customize/{tableId}/debate")
                    .then().statusCode(200);
        }

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND"})
        void 사용자_지정_토론_진행_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(customizeService).updateUsedAt(eq(tableId), any());

            var document = document("customize/get", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().patch("/api/table/customize/{tableId}/debate")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class DeleteTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.CUSTOMIZE_API)
                .summary("사용자 지정 토론 시간표 삭제")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                );

        @Test
        void 사용자_지정_테이블_삭제_성공() {
            long tableId = 5L;
            doNothing().when(customizeService).deleteTable(eq(tableId), any());

            var document = document("customize/delete", 204)
                    .request(requestDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().delete("/api/table/customize/{tableId}")
                    .then().statusCode(204);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND"})
        @ParameterizedTest
        void 사용자_지정_테이블_삭제_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(customizeService).deleteTable(eq(tableId), any());

            var document = document("customize/delete", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", tableId)
                    .when().delete("/api/table/customize/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
