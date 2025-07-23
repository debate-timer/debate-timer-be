package com.debatetimer.controller.poll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.dto.poll.response.PollCreateResponse;
import com.debatetimer.dto.poll.response.PollInfoResponse;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class PollDocumentTest extends BaseDocumentTest {

    @Nested
    class CreatePoll {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.POLL_API)
                .summary("선거 생성")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("테이블 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("선거 ID")
                );

        @Test
        void 선거_생성_성공() {
            PollCreateResponse response = new PollCreateResponse(1l);
            doReturn(response).when(pollService).create(anyLong(), any(Member.class));

            var document = document("poll/post", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", 1l)
                    .when().post("/api/polls/{tableId}")
                    .then().statusCode(201);
        }
    }

    @Nested
    class ReadPollInfo {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.POLL_API)
                .summary("선거 정보 조회")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("pollId").description("선거 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("선거 ID"),
                        fieldWithPath("status").type(STRING).description("선거 상태 - 진행중 : PROGRESS, 완료 : DONE"),
                        fieldWithPath("prosTeamName").type(STRING).description("찬성측 팀 이름"),
                        fieldWithPath("consTeamName").type(STRING).description("반대측 팀 이름"),
                        fieldWithPath("totalCount").type(NUMBER).description("전체 투표 수"),
                        fieldWithPath("prosCount").type(NUMBER).description("찬성 투표 수"),
                        fieldWithPath("consCount").type(NUMBER).description("반대 투표 수")
                );

        @Test
        void 선거_정보_조회() {
            PollInfoResponse response = new PollInfoResponse(
                    1L,
                    PollStatus.PROGRESS,
                    "찬성",
                    "반대",
                    3L,
                    2L,
                    1L
            );
            doReturn(response).when(pollService).readPollInfo(anyLong(), any(Member.class));

            var document = document("poll/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("pollId", 1l)
                    .when().get("/api/polls/{pollId}")
                    .then().statusCode(200);
        }
    }

    @Nested
    class UpdateToDone {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.POLL_API)
                .summary("선거 완료")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("pollId").description("선거 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("선거 ID"),
                        fieldWithPath("status").type(STRING).description("선거 상태 - 진행중 : PROGRESS, 완료 : DONE"),
                        fieldWithPath("prosTeamName").type(STRING).description("찬성측 팀 이름"),
                        fieldWithPath("consTeamName").type(STRING).description("반대측 팀 이름"),
                        fieldWithPath("totalCount").type(NUMBER).description("전체 투표 수"),
                        fieldWithPath("prosCount").type(NUMBER).description("찬성 투표 수"),
                        fieldWithPath("consCount").type(NUMBER).description("반대 투표 수")
                );

        @Test
        void 선거_완료() {
            PollInfoResponse response = new PollInfoResponse(
                    1L,
                    PollStatus.PROGRESS,
                    "찬성",
                    "반대",
                    3L,
                    2L,
                    1L
            );
            doReturn(response).when(pollService).updateToDone(anyLong(), any(Member.class));

            var document = document("poll/patch", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("pollId", 1l)
                    .when().patch("/api/polls/{pollId}")
                    .then().statusCode(200);
        }
    }
}
