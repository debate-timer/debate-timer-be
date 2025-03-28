package com.debatetimer.service;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.CustomizeTimeBoxGenerator;
import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.ParliamentaryTableGenerator;
import com.debatetimer.fixture.ParliamentaryTimeBoxGenerator;
import com.debatetimer.fixture.TimeBasedTableGenerator;
import com.debatetimer.fixture.TimeBasedTimeBoxGenerator;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import com.debatetimer.repository.timebased.TimeBasedTableRepository;
import com.debatetimer.repository.timebased.TimeBasedTimeBoxRepository;
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
    protected ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository;

    @Autowired
    protected TimeBasedTableRepository timeBasedTableRepository;

    @Autowired
    protected TimeBasedTimeBoxRepository timeBasedTimeBoxRepository;

    @Autowired
    protected CustomizeTableRepository customizeTableRepository;

    @Autowired
    protected CustomizeTimeBoxRepository customizeTimeBoxRepository;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected ParliamentaryTableGenerator parliamentaryTableGenerator;

    @Autowired
    protected ParliamentaryTimeBoxGenerator parliamentaryTimeBoxGenerator;

    @Autowired
    protected TimeBasedTableGenerator timeBasedTableGenerator;

    @Autowired
    protected TimeBasedTimeBoxGenerator timeBasedTimeBoxGenerator;

    @Autowired
    protected CustomizeTableGenerator customizeTableGenerator;

    @Autowired
    protected CustomizeTimeBoxGenerator customizeTimeBoxGenerator;
}
