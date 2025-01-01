package com.debatetimer;

import com.debatetimer.fixture.FixtureGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({FixtureGenerator.class})
@DataJpaTest
public abstract class BaseRepositoryTest {

    @Autowired
    protected FixtureGenerator fixtureGenerator;
}
