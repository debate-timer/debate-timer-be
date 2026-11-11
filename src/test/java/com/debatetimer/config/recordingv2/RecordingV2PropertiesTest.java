package com.debatetimer.config.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RecordingV2PropertiesTest {

    @Test
    void 설정하지_않은_값은_기본값을_쓰고_꺼져_있다() {
        RecordingV2Properties properties = new RecordingV2Properties(null, null, null, null, null, null, null, null,
                null);

        assertAll(
                () -> assertThat(properties.enabled()).isFalse(),
                () -> assertThat(properties.collectWindow()).isEqualTo(Duration.ofSeconds(20)),
                () -> assertThat(properties.maxDevices()).isEqualTo(8),
                () -> assertThat(properties.spoolDir()).endsWith("recording-v2"),
                () -> assertThat(properties.workers()).isEqualTo(2)
        );
    }
}
