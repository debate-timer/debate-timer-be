package com.debatetimer;

import com.debatetimer.config.FixtureGeneratorConfig;
import com.debatetimer.fixture.FixtureGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({FixtureGeneratorConfig.class})
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected FixtureGenerator fixtureGenerator;
}
