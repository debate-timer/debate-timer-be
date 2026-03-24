package com.debatetimer.controller;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.client.oauth.OAuthClient;
import com.debatetimer.domain.customize.BellType;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.poll.VoteTeam;
import com.debatetimer.dto.customize.request.BellRequest;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTableInfoCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.dto.poll.request.VoteRequest;
import com.debatetimer.fixture.HeaderGenerator;
import com.debatetimer.fixture.TokenGenerator;
import com.debatetimer.fixture.entity.CustomizeTableEntityGenerator;
import com.debatetimer.fixture.entity.CustomizeTimeBoxEntityGenerator;
import com.debatetimer.fixture.entity.MemberGenerator;
import com.debatetimer.fixture.entity.OrganizationEntityGenerator;
import com.debatetimer.fixture.entity.OrganizationTemplateEntityGenerator;
import com.debatetimer.fixture.entity.PollEntityGenerator;
import com.debatetimer.fixture.entity.VoteEntityGenerator;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
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
    protected CustomizeTableEntityGenerator customizeTableEntityGenerator;

    @Autowired
    protected CustomizeTimeBoxEntityGenerator customizeTimeBoxEntityGenerator;

    @Autowired
    protected PollEntityGenerator pollEntityGenerator;

    @Autowired
    protected VoteEntityGenerator voteEntityGenerator;

    @Autowired
    protected OrganizationEntityGenerator organizationEntityGenerator;

    @Autowired
    protected OrganizationTemplateEntityGenerator organizationTemplateEntityGenerator;

    @Autowired
    protected HeaderGenerator headerGenerator;

    @Autowired
    protected TokenGenerator tokenGenerator;

    @MockitoBean
    protected OAuthClient oAuthClient;

    @LocalServerPort
    private int port;

    private RequestSpecification spec;

    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

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
        return fixtureMonkey.giveMeBuilder(CustomizeTableCreateRequest.class)
                .set("info", getCustomizeTableInfoCreateRequestBuilder().sample())
                .set("table", getCustomizeTimeBoxCreateRequestBuilder().sampleList(2));
    }

    protected ArbitraryBuilder<VoteRequest> getVoteRequestBuilder() {
        return fixtureMonkey.giveMeBuilder(VoteRequest.class)
                .set("name", "콜리")
                .set("team", VoteTeam.PROS)
                .set("participateCode", UUID.randomUUID().toString());
    }

    private ArbitraryBuilder<CustomizeTableInfoCreateRequest> getCustomizeTableInfoCreateRequestBuilder() {
        return fixtureMonkey.giveMeBuilder(CustomizeTableInfoCreateRequest.class)
                .set("name", "자유 테이블")
                .set("agenda", "주제")
                .set("prosTeamName", "찬성")
                .set("consTeamName", "반대")
                .set("warningBell", true)
                .set("finishBell", true);
    }

    private ArbitraryBuilder<CustomizeTimeBoxCreateRequest> getCustomizeTimeBoxCreateRequestBuilder() {
        return fixtureMonkey.giveMeBuilder(CustomizeTimeBoxCreateRequest.class)
                .set("stance", Stance.PROS)
                .set("speechType", "입론1")
                .set("boxType", CustomizeBoxType.NORMAL)
                .set("remainingTime", 120)
                .set("bell", getBellRequestBuilder().sampleList(2))
                .set("timePerTeam", 60)
                .set("timePerSpeaking", null)
                .set("speaker", "발언자");
    }

    private ArbitraryBuilder<BellRequest> getBellRequestBuilder() {
        return fixtureMonkey.giveMeBuilder(BellRequest.class)
                .set("type", BellType.AFTER_START)
                .set("remainingTime", 30)
                .set("count", 1);
    }
}
