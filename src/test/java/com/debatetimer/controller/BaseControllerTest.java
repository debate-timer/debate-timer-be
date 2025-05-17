package com.debatetimer.controller;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.client.oauth.OAuthClient;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTableInfoCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.CustomizeTimeBoxGenerator;
import com.debatetimer.fixture.HeaderGenerator;
import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.TokenGenerator;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
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
    protected CustomizeTableRepository customizeTableRepository;

    @Autowired
    protected MemberGenerator memberGenerator;

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

    protected ArbitraryBuilder<CustomizeTableCreateRequest> getCustomizeTableCreateRequestBuilder() {
        FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build();
        return fixtureMonkey.giveMeBuilder(CustomizeTableCreateRequest.class)
                .set("info", getCustomizeTableInfoCreateRequestBuilder().sample())
                .set("table", getCustomizeTimeBoxCreateRequestBuilder().sampleList(2));
    }

    private ArbitraryBuilder<CustomizeTableInfoCreateRequest> getCustomizeTableInfoCreateRequestBuilder() {
        FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build();
        return fixtureMonkey.giveMeBuilder(CustomizeTableInfoCreateRequest.class)
                .set("name", "자유 테이블")
                .set("agenda", "주제")
                .set("prosTeamName", "찬성")
                .set("consTeamName", "반대")
                .set("warningBell", true)
                .set("finishBell", true);
    }

    private ArbitraryBuilder<CustomizeTimeBoxCreateRequest> getCustomizeTimeBoxCreateRequestBuilder() {
        FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build();
        return fixtureMonkey.giveMeBuilder(CustomizeTimeBoxCreateRequest.class)
                .set("stance", Stance.PROS)
                .set("speechType", "입론1")
                .set("boxType", CustomizeBoxType.NORMAL)
                .set("time", 120)
                .set("timePerTeam", 60)
                .set("timePerSpeaking", null)
                .set("speaker", "발언자");
    }
}
