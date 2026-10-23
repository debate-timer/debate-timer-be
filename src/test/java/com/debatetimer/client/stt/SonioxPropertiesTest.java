package com.debatetimer.client.stt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SonioxPropertiesTest {

    @Test
    void 설정하지_않은_값은_기본값을_쓴다() {
        SonioxProperties properties = new SonioxProperties("key", null, null, null, null, null, null, null);

        assertAll(
                () -> assertThat(properties.model()).isEqualTo("stt-async-v5"),
                () -> assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(10)),
                () -> assertThat(properties.maxConcurrent()).isEqualTo(4)
        );
    }
}
