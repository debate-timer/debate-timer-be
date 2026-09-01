package com.debatetimer.controller.sharing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.debatetimer.domain.customize.BellType;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.response.BellResponse;
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

public class SharingDocumentTest extends BaseDocumentTest {

    @Nested
    class IssueChairmanToken {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.SHARING_API)
                .summary("사회자용 토큰 발급")
                .requestHeader(headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰"))
                .pathParameter(parameterWithName("tableId").description("테이블 id"));

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("chairmanToken").type(STRING).description("사회자용 토큰")
                );

        @Test
        void 사회자_용_토큰_생성_성공() {
            long requestTableId = 1L;
            long debateTime = 500L;
            doReturn(debateTime).when(customizeService)
                    .findDebateTime(eq(requestTableId), any(Member.class));
            doReturn("testToken").when(authManager)
                    .issueChairmanToken(any(Member.class), eq(debateTime * 2));

            var document = document("sharing/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", String.valueOf(requestTableId))
                    .when().get("/api/live/{tableId}/chairman-token")
                    .then().statusCode(200);
        }
    }

    @Nested
    class GetTable {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.SHARING_API)
                .summary("비회원용 사용자 지정 토론 시간표 조회")
                .description("""
                        ### 타임 박스 종류에 따른 웅답 값
                        | 타임 박스 종류 | 필수 입력 | 선택 입력 | null 입력 |
                        | :---: | ---| --- | --- |
                        | 커스텀 타임 박스 | stance, speechType, boxType, time | speaker | timePerTeam, timePerSpeaking |
                        | 자유 토론 타임 박스 | stance, speechType, boxType, timePerTeam | speaker, timePerSpeaking | time |
                        """)
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
                        fieldWithPath("table[].bell").type(ARRAY).description("종소리 정보").optional(),
                        fieldWithPath("table[].bell[].type").type(STRING).description("종소리 종류"),
                        fieldWithPath("table[].bell[].time").type(NUMBER).description("종소리 울릴 시간(초)"),
                        fieldWithPath("table[].bell[].count").type(NUMBER).description("종소리 횟수"),
                        fieldWithPath("table[].timePerTeam").type(NUMBER).description("팀당 발언 시간 (초)").optional(),
                        fieldWithPath("table[].timePerSpeaking").type(NUMBER).description("1회 발언 시간 (초)").optional(),
                        fieldWithPath("table[].speaker").type(STRING).description("발언자 이름").optional()
                );

        @Test
        void 비회원_사용자_지정_테이블_조회_성공() {
            long tableId = 5L;
            CustomizeTableResponse response = new CustomizeTableResponse(
                    5L,
                    new CustomizeTableInfoResponse("나의 테이블", TableType.CUSTOMIZE, "토론 주제",
                            "찬성", "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxResponse(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                                    120, List.of(new BellResponse(BellType.AFTER_START, 90, 1)), null, null, "콜리"),
                            new CustomizeTimeBoxResponse(Stance.CONS, "입론", CustomizeBoxType.NORMAL,
                                    120, List.of(new BellResponse(BellType.AFTER_START, 90, 1),
                                    new BellResponse(BellType.AFTER_START, 120, 2)), null, null, "비토"),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "난상 토론", CustomizeBoxType.TIME_BASED,
                                    null, null, 360, 120, null),
                            new CustomizeTimeBoxResponse(Stance.NEUTRAL, "존중 토론", CustomizeBoxType.TIME_BASED,
                                    null, null, 360, null, null)
                    )
            );
            doReturn(response).when(customizeService).findTable(eq(tableId));

            var document = document("sharing/get_table", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", tableId)
                    .when().get("/api/live/table/customize/{tableId}")
                    .then().statusCode(200);
        }

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND"})
        void 비회원_사용자_지정_테이블_조회_실패(ClientErrorCode errorCode) {
            long tableId = 5L;
            doThrow(new DTClientErrorException(errorCode)).when(customizeService).findTable(eq(tableId));

            var document = document("sharing/get_table", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", tableId)
                    .when().get("/api/live/table/customize/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
