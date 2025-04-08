package com.debatetimer.repository;

import com.debatetimer.config.JpaAuditingConfig;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.CustomizeTimeBoxGenerator;
import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.fixture.ParliamentaryTableGenerator;
import com.debatetimer.fixture.ParliamentaryTimeBoxGenerator;
import com.debatetimer.fixture.TimeBasedTableGenerator;
import com.debatetimer.fixture.TimeBasedTimeBoxGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({
        JpaAuditingConfig.class,
        MemberGenerator.class,
        ParliamentaryTableGenerator.class,
        ParliamentaryTimeBoxGenerator.class,
        TimeBasedTableGenerator.class,
        TimeBasedTimeBoxGenerator.class,
        CustomizeTableGenerator.class,
        CustomizeTimeBoxGenerator.class
})
@DataJpaTest
public abstract class BaseRepositoryTest {

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
