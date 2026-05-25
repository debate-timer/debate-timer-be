package com.debatetimer.controller.sharing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.member.Member;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
}
