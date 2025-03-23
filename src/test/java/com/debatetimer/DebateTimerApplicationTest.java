package com.debatetimer;

import com.debatetimer.client.ChannelNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DebateTimerApplicationTest {

    @MockitoBean
    private ChannelNotifier channelNotifier;

    @Test
    void contextLoads() {
    }
}
