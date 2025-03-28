package com.debatetimer;

import com.debatetimer.client.ErrorNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DebateTimerApplicationTest {

    @MockitoBean
    private ErrorNotifier errorNotifier;

    @Test
    void contextLoads() {
    }
}
