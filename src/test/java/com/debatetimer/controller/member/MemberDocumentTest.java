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
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
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
        void 회원_생성_성공() {
            MemberCreateRequest request = new MemberCreateRequest("dfsfgdsg", "http://redirectUrl");
            MemberInfo memberInfo = new MemberInfo(EXIST_MEMBER_EMAIL);
            MemberCreateResponse response = new MemberCreateResponse(EXIST_MEMBER_ID, EXIST_MEMBER_EMAIL);
            doReturn(memberInfo).when(authService).getMemberInfo(request);
            doReturn(response).when(memberService).createMember(memberInfo);
            doReturn(EXIST_MEMBER_TOKEN_RESPONSE).when(authManager).issueToken(memberInfo);
            doReturn(EXIST_MEMBER_COOKIE).when(cookieManager).createRefreshTokenCookie(EXIST_MEMBER_REFRESH_TOKEN);

            var document = document("member/create", 201).request(requestDocument).response(responseDocument).build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201);
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
                fieldWithPath("tables[].duration").type(NUMBER).description("소요 시간 (초)"));

        @Test
        void 테이블_조회_성공() {
            TableResponses response = new TableResponses(
                    List.of(new TableResponse(1L, "토론 테이블 1", TableType.PARLIAMENTARY, 1800),
                            new TableResponse(2L, "토론 테이블 2", TableType.PARLIAMENTARY, 2000))
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
            doReturn(EXIST_MEMBER_COOKIE).when(cookieManager).createRefreshTokenCookie(any());

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

        @EnumSource(value = ClientErrorCode.class, names = {"EMPTY_COOKIE"})
        @ParameterizedTest
        void 토큰_갱신_실패_쿠키_추출(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(cookieManager).extractRefreshToken(any());

            var document = document("member/reissue", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .cookie("refreshToken")
                    .when().post("/api/member/reissue")
                    .then().statusCode(errorCode.getStatus().value());
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
            doReturn(DELETE_MEMBER_COOKIE).when(cookieManager).deleteRefreshTokenCookie();

            var document = document("member/logout", 204)
                    .request(requestDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .cookie("refreshToken")
                    .when().post("/api/member/logout")
                    .then().statusCode(204);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"EMPTY_COOKIE"})
        @ParameterizedTest
        void 로그아웃_실패_쿠키_추출(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(cookieManager).extractRefreshToken(any());

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
