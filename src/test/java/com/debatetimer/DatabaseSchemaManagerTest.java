package com.debatetimer;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.debatetimer.client.ChannelNotifier;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("flyway")
class DatabaseSchemaManagerTest {

    @Autowired
    private Flyway flyway;

    @MockitoBean
    private ChannelNotifier channelNotifier;

    @Test
    void contextLoads() {
        assertThatCode(() -> flyway.validate()).doesNotThrowAnyException();
    }
}
