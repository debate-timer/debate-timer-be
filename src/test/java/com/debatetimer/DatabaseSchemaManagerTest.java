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
        /*
        * 테스트 기능 1 : Schema Validation
        * 테스트 기능 2 : SQL 문법 검사 (H2 기준)
        * 한계 : H2 에서 통과하면 통과 해버림
        * */
        assertThatCode(() -> flyway.validate()).doesNotThrowAnyException();
    }
}
