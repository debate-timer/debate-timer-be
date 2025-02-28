package com.debatetimer;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("flyway")
public class DatabaseSchemaManagerTest {

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
        assertThatCode(() -> flyway.validate()).doesNotThrowAnyException();
    }
}
