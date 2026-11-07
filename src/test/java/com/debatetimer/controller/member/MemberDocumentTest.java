package com.debatetimer.controller.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberFeatureFlagsResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponse;
import com.debatetimer.dto.member.TableResponses;
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

public class MemberDocumentTest extends BaseDocumentTest {

    @Nested
    class CreateMember {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .summary("멤버 생성")
                .requestBodyField(
                        fieldWithPath("code").type(STRING).description("인가 코드"),
                        fieldWithPath("redirectUrl").type(STRING).description("리다이렉트 URL")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("멤버 ID"),
                        fieldWithPath("email").type(STRING).description("멤버 이메일")
                );

        @Test
        void 회원_생성_및_로그인_성공() {
            MemberCreateRequest request = new MemberCreateRequest("dfsfgdsg", "http://localhost:3000");
            MemberInfo memberInfo = new MemberInfo(EXIST_MEMBER_EMAIL);
            MemberCreateResponse response = new MemberCreateResponse(EXIST_MEMBER_ID, EXIST_MEMBER_EMAIL);
            doReturn(memberInfo).when(authService).getMemberInfo(request);
            doReturn(response).when(memberService).createMember(memberInfo);
            doReturn(EXIST_MEMBER_TOKEN_RESPONSE).when(authManager).issueToken(memberInfo);
            doReturn(responseCookie(EXIST_MEMBER_REFRESH_TOKEN, 500)).when(cookieManager)
                    .createCookie(any(), any(), any());

            var document = document("member/create", 201).request(requestDocument).response(responseDocument).build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"MEMBER_NOT_FOUND"}) // PR #160 병합 시 변경
        @ParameterizedTest
        void 회원_생성_및_로그인_실패(ClientErrorCode errorCode) {
            MemberCreateRequest request = new MemberCreateRequest("dfsfgdsg", "http://localhost:3000");
            doThrow(new DTClientErrorException(errorCode)).when(authService).getMemberInfo(request);

            var document = document("member/create", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class GetFeatureFlags {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .summary("회원의 피쳐 플래그 조회")
                .description("""
                        로그인한 회원에게 부여된 피쳐 플래그 목록을 조회합니다. 플래그가 없으면 빈 배열입니다.
                        프론트는 이 목록으로 플래그가 필요한 기능의 화면 노출 여부를 정합니다.

                        ### 피쳐 플래그
                        | 플래그 | 설명 | 필요한 API |
                        | :---: | --- | --- |
                        | TRANSCRIPT | 속기록 · 토론 분석 | `POST /api/stt/soniox`, `POST /api/transcripts/{tableId}`, `GET /api/transcripts`, `GET /api/transcripts/{transcriptId}` |

                        플래그는 어드민에서 회원별로 부여 · 회수하므로, 앱 진입이나 로그인 직후처럼 화면을 그리기 전에 다시 조회하는 것을 권장합니다.
                        플래그가 필요한 API 는 Swagger 요약 앞에 `[플래그 이름]` 이 붙어 있고, 플래그 없이 호출하면 `403 FEATURE_NOT_ALLOWED` 를 응답합니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                );

        private final RestDocumentationResponse responseDocument = response().responseBodyField(
                fieldWithPath("featureFlags").type(ARRAY).description("회원에게 부여된 피쳐 플래그 (TRANSCRIPT)"));

        @Test
        void 피쳐_플래그_조회_성공() {
            MemberFeatureFlagsResponse response = new MemberFeatureFlagsResponse(List.of(FeatureFlag.TRANSCRIPT));
            doReturn(response).when(memberService).getFeatureFlags(EXIST_MEMBER_ID);

            var document = document("member/feature-flags", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().get("/api/member/feature-flags")
                    .then().statusCode(200);
        }
    }

    @Nested
    class GetTables {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .summary("멤버의 토론 시간표 조회")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                );

        private final RestDocumentationResponse responseDocument = response().responseBodyField(
                fieldWithPath("tables").type(ARRAY).description("멤버의 토론 테이블들"),
                fieldWithPath("tables[].id").type(NUMBER).description("토론 테이블 ID (토론 타입 별로 ID를 가짐)"),
                fieldWithPath("tables[].name").type(STRING).description("토론 테이블 이름"),
                fieldWithPath("tables[].type").type(STRING).description("토론 타입"),
                fieldWithPath("tables[].agenda").type(STRING).description("토론 주제"));

        @Test
        void 테이블_조회_성공() {
            TableResponses response = new TableResponses(
                    List.of(new TableResponse(1L, "토론 테이블 1", TableType.CUSTOMIZE, "주제1"),
                            new TableResponse(2L, "토론 테이블 2", TableType.CUSTOMIZE, "주제2"))
            );
            doReturn(response).when(memberService).getTables(EXIST_MEMBER_ID);

            var document = document("member/table", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().get("/api/table")
                    .then().statusCode(200);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"MEMBER_NOT_FOUND"})
        @ParameterizedTest
        void 테이블_조회_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(memberService).getTables(EXIST_MEMBER_ID);

            var document = document("member/table", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().get("/api/table")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class ReissueAccessToken {
        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .summary("토큰 갱신")
                .requestCookie(
                        cookieWithName("refreshToken").description("리프레시 토큰")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .responseCookie(
                        cookieWithName("refreshToken").description("리프레시 토큰")
                );

        @Test
        void 토큰_갱신_성공() {
            doReturn(EXIST_MEMBER_TOKEN_RESPONSE).when(authManager).reissueToken(any());
            doReturn(responseCookie(EXIST_MEMBER_REFRESH_TOKEN, 500)).when(cookieManager)
                    .createCookie(any(), any(), any());

            var document = document("member/logout", 204)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .cookie("refreshToken")
                    .when().post("/api/member/reissue")
                    .then().statusCode(200);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"EXPIRED_TOKEN", "UNAUTHORIZED_MEMBER"})
        @ParameterizedTest
        void 토큰_갱신_실패_토큰_갱신(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(authManager).reissueToken(any());

            var document = document("member/reissue", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .cookie("refreshToken")
                    .when().post("/api/member/reissue")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class Logout {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .summary("로그아웃")
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .requestCookie(
                        cookieWithName("refreshToken").description("리프레시 토큰")
                );

        @Test
        void 로그아웃_성공() {
            doReturn(responseCookie(EXIST_MEMBER_REFRESH_TOKEN, 0)).when(cookieManager).createExpiredCookie(any());

            var document = document("member/logout", 204)
                    .request(requestDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .cookie("refreshToken")
                    .when().post("/api/member/logout")
                    .then().statusCode(204);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"UNAUTHORIZED_MEMBER", "EXPIRED_TOKEN"})
        @ParameterizedTest
        void 로그아웃_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(authService).logout(any(), any());

            var document = document("member/logout", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .cookie("refreshToken")
                    .when().post("/api/member/logout")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
