package com.debatetimer;

import com.debatetimer.fixture.DtoGenerator;
import com.debatetimer.fixture.FixtureGenerator;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BaseServiceTest {

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ParliamentaryTableRepository parliamentaryTableRepository;

    @Autowired
    protected ParliamentaryTimeBoxRepository timeBoxRepository;

    @Autowired
    protected FixtureGenerator fixtureGenerator;

    @Autowired
    protected DtoGenerator dtoGenerator;
}
