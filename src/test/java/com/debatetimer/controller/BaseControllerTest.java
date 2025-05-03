package com.debatetimer.controller;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.client.oauth.OAuthClient;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.CustomizeTimeBoxGenerator;
import com.debatetimer.fixture.HeaderGenerator;
import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.ParliamentaryTableGenerator;
import com.debatetimer.fixture.ParliamentaryTimeBoxGenerator;
import com.debatetimer.fixture.TokenGenerator;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseControllerTest {

    @Autowired
    protected ParliamentaryTableRepository parliamentaryTableRepository;

    @Autowired
    protected CustomizeTableRepository customizeTableRepository;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected ParliamentaryTableGenerator parliamentaryTableGenerator;

    @Autowired
    protected ParliamentaryTimeBoxGenerator parliamentaryTimeBoxGenerator;

    @Autowired
    protected CustomizeTableGenerator customizeTableGenerator;

    @Autowired
    protected CustomizeTimeBoxGenerator customizeTimeBoxGenerator;

    @Autowired
    protected HeaderGenerator headerGenerator;

    @Autowired
    protected TokenGenerator tokenGenerator;

    @MockitoBean
    protected OAuthClient oAuthClient;

    @LocalServerPort
    private int port;

    private RequestSpecification spec;

    @BeforeEach
    void setEnvironment() {
        RestAssured.port = port;
        spec = new RequestSpecBuilder()
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    protected RequestSpecification given() {
        return RestAssured.given(spec);
    }
}
