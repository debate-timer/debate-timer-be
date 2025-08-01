package com.debatetimer.repository;

import com.debatetimer.config.JpaAuditingConfig;
import com.debatetimer.fixture.entity.BellEntityGenerator;
import com.debatetimer.fixture.entity.CustomizeTableEntityGenerator;
import com.debatetimer.fixture.entity.CustomizeTimeBoxEntityGenerator;
import com.debatetimer.fixture.entity.MemberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({
        JpaAuditingConfig.class,
        MemberGenerator.class,
        CustomizeTableEntityGenerator.class,
        CustomizeTimeBoxEntityGenerator.class,
        BellEntityGenerator.class,
})
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected CustomizeTableEntityGenerator customizeTableEntityGenerator;

    @Autowired
    protected CustomizeTimeBoxEntityGenerator customizeTimeBoxEntityGenerator;

    @Autowired
    protected BellEntityGenerator bellEntityGenerator;
}
