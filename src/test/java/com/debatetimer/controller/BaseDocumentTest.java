package com.debatetimer.controller;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.HeaderGenerator;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.service.member.MemberService;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Headers;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentation;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentationConfigurer;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseDocumentTest {

    protected static long EXIST_MEMBER_ID = 123L;
    protected static Member EXIST_MEMBER = new Member(EXIST_MEMBER_ID, "존재하는 멤버");

    protected static RestDocumentationResponse ERROR_RESPONSE = new RestDocumentationResponse()
            .responseBodyField(
                    fieldWithPath("message").type(STRING).description("에러 메시지")
            );

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    protected MemberService memberService;

    @MockitoBean
    protected ParliamentaryService parliamentaryService;

    @Autowired
    private HeaderGenerator headerGenerator;

    protected Headers existMemberHeaders;

    @LocalServerPort
    private int port;

    private RequestSpecification spec;

    @BeforeEach
    void setEnvironment(RestDocumentationContextProvider restDocumentation) {
        setRestAssured(restDocumentation);
        setLoginMember();
        setHeaders();
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
        when(memberRepository.getById(EXIST_MEMBER_ID)).thenReturn(EXIST_MEMBER);
    }

    private void setHeaders() {
        existMemberHeaders = headerGenerator.generateAccessToken(EXIST_MEMBER);
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
