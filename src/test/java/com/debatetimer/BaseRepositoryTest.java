package com.debatetimer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


@ExtendWith(DataBaseCleaner.class)
@DataJpaTest
public abstract class BaseRepositoryTest {
}
