package com.debatetimer;

import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.service.member.MemberService;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentation;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentationConfigurer;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseDocumentTest {

    protected static String EXIST_MEMBER_ID = "1";

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    protected MemberService memberService;

    @MockitoBean
    protected ParliamentaryService parliamentaryService;

    @LocalServerPort
    private int port;

    private RequestSpecification spec;

    @BeforeEach
    void setEnvironment(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;

        RestAssuredRestDocumentationConfigurer webConfigurer =
                RestAssuredRestDocumentation.documentationConfiguration(restDocumentation);
        spec = new RequestSpecBuilder()
                .addFilter(webConfigurer)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    protected RestDocumentationFilterBuilder document(String identifier) {
        return new RestDocumentationFilterBuilder(identifier);
    }

    protected RequestSpecification given(RestDocumentationFilter documentationFilter) {
        return RestAssured.given(spec)
                .filter(documentationFilter);
    }
}
