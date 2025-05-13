package com.debatetimer.repository;

import com.debatetimer.config.JpaAuditingConfig;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.CustomizeTimeBoxGenerator;
import com.debatetimer.fixture.MemberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({
        JpaAuditingConfig.class,
        MemberGenerator.class,
        CustomizeTableGenerator.class,
        CustomizeTimeBoxGenerator.class
})
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected CustomizeTableGenerator customizeTableGenerator;

    @Autowired
    protected CustomizeTimeBoxGenerator customizeTimeBoxGenerator;
}
