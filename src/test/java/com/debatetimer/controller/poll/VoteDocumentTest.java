package com.debatetimer.controller.poll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.dto.poll.request.VoteRequest;
import com.debatetimer.dto.poll.response.VoteCreateResponse;
import com.debatetimer.dto.poll.response.VoterPollInfoResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class VoteDocumentTest extends BaseDocumentTest {

    @Nested
    class GetVotersPollInfo {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.POLL_API)
                .summary("투표자 - 선거 정보 조회")
                .pathParameter(
                        parameterWithName("pollId").description("선거 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("선거 ID"),
                        fieldWithPath("status").type(STRING).description("선거 상태 - 진행중 : PROGRESS, 완료 : DONE"),
                        fieldWithPath("prosTeamName").type(STRING).description("찬성측 팀 이름"),
                        fieldWithPath("consTeamName").type(STRING).description("반대측 팀 이름"),
                        fieldWithPath("participateCode").type(STRING).description("참여 코드"),
                        fieldWithPath("totalCount").type(NUMBER).description("전체 투표 수"),
                        fieldWithPath("prosCount").type(NUMBER).description("찬성 투표 수"),
                        fieldWithPath("consCount").type(NUMBER).description("반대 투표 수")
                );

        @Test
        void 투표자_선거_정보_조회() {
            VoterPollInfoResponse response = new VoterPollInfoResponse(
                    1L,
                    PollStatus.PROGRESS,
                    "찬성",
                    "반대",
                    UUID.randomUUID().toString(),
                    3L,
                    2L,
                    1L
            );
            doReturn(response).when(voteService).getVoterPollInfo(anyLong());

            var document = document("vote/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .pathParam("pollId", 1l)
                    .when().get("/api/polls/{pollId}/votes")
                    .then().statusCode(200);
        }
    }

    @Nested
    class VotePoll {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.POLL_API)
                .summary("투표자 - 선거 투표")
                .pathParameter(
                        parameterWithName("pollId").description("선거 ID")
                )
                .requestBodyField(
                        fieldWithPath("name").type(STRING).description("투표자 이름"),
                        fieldWithPath("participateCode").type(STRING).description("투표 참여 코드"),
                        fieldWithPath("team").type(STRING).description("투표 팀")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("투표 ID"),
                        fieldWithPath("name").type(STRING).description("투표자 이름"),
                        fieldWithPath("participateCode").type(STRING).description("투표 참여 코드"),
                        fieldWithPath("team").type(STRING).description("투표 팀")
                );

        @Test
        void 투표자_선거_정보_조회() {
            VoteRequest voteRequest = new VoteRequest("콜리", UUID.randomUUID().toString(), VoteTeam.PROS);
            VoteCreateResponse response = new VoteCreateResponse(
                    1L,
                    voteRequest.name(),
                    voteRequest.participateCode(),
                    voteRequest.team()
            );
            doReturn(response).when(voteService).vote(anyLong(), any());

            var document = document("vote/post", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", 1l)
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(201);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {
                        "ALREADY_DONE_POLL",
                        "ALREADY_VOTED_PARTICIPANT",
                        "INVALID_POLL_PARTICIPANT_CODE",
                        "INVALID_POLL_PARTICIPANT_NAME"
                }
        )
        @ParameterizedTest
        void 투표자_투표_실패(ClientErrorCode clientErrorCode) {
            VoteRequest voteRequest = new VoteRequest("콜리", UUID.randomUUID().toString(), VoteTeam.PROS);
            doThrow(new DTClientErrorException(clientErrorCode)).when(voteService).vote(anyLong(), any());

            var document = document("vote/post", clientErrorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(voteRequest)
                    .pathParam("pollId", 1l)
                    .when().post("/api/polls/{pollId}/votes")
                    .then().statusCode(clientErrorCode.getStatus().value());

        }
    }
}
