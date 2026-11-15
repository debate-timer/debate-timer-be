package com.debatetimer.config.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RecordingV2PropertiesTest {

    @Test
    void 설정하지_않은_값은_기본값을_쓰고_꺼져_있다() {
        RecordingV2Properties properties = new RecordingV2Properties(null, null, null, null, null, null, null, null,
                null, null, null);

        assertAll(
                () -> assertThat(properties.enabled()).isFalse(),
                () -> assertThat(properties.collectWindow()).isEqualTo(Duration.ofSeconds(20)),
                () -> assertThat(properties.maxDevices()).isEqualTo(8),
                () -> assertThat(properties.deviceIdleTimeout()).isEqualTo(Duration.ofMinutes(10)),
                () -> assertThat(properties.spoolDir()).endsWith("recording-v2"),
                () -> assertThat(properties.workers()).isEqualTo(2),
                () -> assertThat(properties.processingTimeout()).isEqualTo(Duration.ofMinutes(5))
        );
    }

    @Test
    void 무중단_배포로_함께_뜬_앱이_스풀_디렉터리를_나눠_쓰도록_포트별_디렉터리를_쓴다() {
        RecordingV2Properties properties = new RecordingV2Properties(null, null, null, null, "/spool", null, null,
                null, null, null, null);

        assertAll(
                () -> assertThat(properties.spoolDirectory("8080")).isEqualTo(Path.of("/spool", "port-8080")),
                () -> assertThat(properties.spoolDirectory("8081")).isEqualTo(Path.of("/spool", "port-8081"))
        );
    }
}
