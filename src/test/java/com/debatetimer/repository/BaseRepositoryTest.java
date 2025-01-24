package com.debatetimer.repository;

import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.ParliamentaryTableGenerator;
import com.debatetimer.fixture.ParliamentaryTimeBoxGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({MemberGenerator.class, ParliamentaryTableGenerator.class, ParliamentaryTimeBoxGenerator.class})
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected ParliamentaryTableGenerator tableGenerator;

    @Autowired
    protected ParliamentaryTimeBoxGenerator timeBoxGenerator;
}
