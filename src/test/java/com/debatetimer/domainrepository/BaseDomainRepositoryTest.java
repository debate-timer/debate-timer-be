package com.debatetimer.domainrepository;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.PollGenerator;
import com.debatetimer.fixture.VoteGenerator;
import com.debatetimer.repository.poll.PollRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BaseDomainRepositoryTest {

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected CustomizeTableGenerator customizeTableGenerator;

    @Autowired
    protected PollGenerator pollGenerator;

    @Autowired
    protected VoteGenerator voteGenerator;

    @Autowired
    protected PollRepository pollRepository;
}
