package com.debatetimer;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles({"test", "flyway"})
class DatabaseSchemaManagerTest {

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
        assertThatCode(() -> flyway.validate()).doesNotThrowAnyException();
    }
}
