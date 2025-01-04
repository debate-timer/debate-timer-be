package com.debatetimer;

import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.ParliamentaryTableGenerator;
import com.debatetimer.fixture.ParliamentaryTimeBoxGenerator;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseControllerTest {

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ParliamentaryTableRepository parliamentaryTableRepository;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected ParliamentaryTableGenerator tableGenerator;

    @Autowired
    protected ParliamentaryTimeBoxGenerator timeBoxGenerator;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }
}
