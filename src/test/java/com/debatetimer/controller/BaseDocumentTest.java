package com.debatetimer.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.debatetimer.controller.tool.cookie.CookieManager;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.auth.AuthService;
import com.debatetimer.service.member.MemberService;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.RequestSpecification;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentation;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentationConfigurer;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseDocumentTest {

    protected static long EXIST_MEMBER_ID = 123L;
    protected static String EXIST_MEMBER_EMAIL = "abcde@gmail.com";
    protected static Member EXIST_MEMBER = new Member(EXIST_MEMBER_ID, EXIST_MEMBER_EMAIL);
    protected static String EXIST_MEMBER_ACCESS_TOKEN = "dflskgnkds";
    protected static String EXIST_MEMBER_REFRESH_TOKEN = "dfsfsdgrs";
    protected static JwtTokenResponse EXIST_MEMBER_TOKEN_RESPONSE = new JwtTokenResponse(EXIST_MEMBER_ACCESS_TOKEN,
            EXIST_MEMBER_REFRESH_TOKEN);
    protected static Headers EXIST_MEMBER_HEADER = new Headers(
            new Header(HttpHeaders.AUTHORIZATION, EXIST_MEMBER_ACCESS_TOKEN));
    protected static Cookie EXIST_MEMBER_COOKIE = new Cookie("refreshToken", EXIST_MEMBER_REFRESH_TOKEN);
    protected static Cookie DELETE_MEMBER_COOKIE = new Cookie("refreshToken", "");

    protected static RestDocumentationResponse ERROR_RESPONSE = new RestDocumentationResponse()
            .responseBodyField(
                    fieldWithPath("message").type(STRING).description("에러 메시지")
            );

    @MockitoBean
    protected MemberService memberService;

    @MockitoBean
    protected ParliamentaryService parliamentaryService;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected AuthManager authManager;

    @MockitoBean
    protected CookieManager cookieManager;

    @LocalServerPort
    private int port;

    private RequestSpecification spec;

    @BeforeEach
    void setEnvironment(RestDocumentationContextProvider restDocumentation) {
        setRestAssured(restDocumentation);
        setLoginMember();
    }

    private void setRestAssured(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;
        RestAssuredRestDocumentationConfigurer webConfigurer =
                RestAssuredRestDocumentation.documentationConfiguration(restDocumentation);
        spec = new RequestSpecBuilder()
                .addFilter(webConfigurer)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    private void setLoginMember() {
        doReturn(EXIST_MEMBER_EMAIL).when(authManager).resolveAccessToken(EXIST_MEMBER_ACCESS_TOKEN);
        doReturn(EXIST_MEMBER).when(authService).getMember(EXIST_MEMBER_EMAIL);
    }

    protected ResponseCookie responseCookie(String token, int maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .path("/")
                .maxAge(maxAge)
                .sameSite("None")
                .secure(true)
                .build();
    }

    protected RestDocumentationRequest request() {
        return new RestDocumentationRequest();
    }

    protected RestDocumentationResponse response() {
        return new RestDocumentationResponse();
    }

    protected RestDocumentationFilterBuilder document(String identifierPrefix, int status) {
        return new RestDocumentationFilterBuilder(identifierPrefix, Integer.toString(status));
    }

    protected RestDocumentationFilterBuilder document(String identifierPrefix, ClientErrorCode errorCode) {
        return new RestDocumentationFilterBuilder(identifierPrefix, errorCode.name());
    }

    protected RequestSpecification given(RestDocumentationFilter documentationFilter) {
        return RestAssured.given(spec)
                .filter(documentationFilter);
    }
}
