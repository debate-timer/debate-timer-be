package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.AudioQualityMetricsV2;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.domain.recordingv2.RecordingDeviceV2;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RecordingSessionRegistryV2Test {

    private static final String SESSION_KEY = "session";
    private static final Duration WINDOW = Duration.ofSeconds(20);

    private MutableClock clock;
    private RecordingSessionRegistryV2 registry;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-12T00:00:00Z"));
        registry = new RecordingSessionRegistryV2(clock);
    }

    @Nested
    class Device {

        @Test
        void 토큰_해시로_참가_기기를_찾는다() {
            registry.addDevice(1L, SESSION_KEY, "host", "host-hash", true, 8);

            assertThat(registry.findDevice(SESSION_KEY, "host-hash")).contains(new RecordingDeviceV2("host", true));
            assertThat(registry.findDevice(SESSION_KEY, "other-hash")).isEmpty();
            assertThat(registry.findDevice("other-session", "host-hash")).isEmpty();
        }

        @Test
        void 최대_기기_수를_넘으면_참가할_수_없다() {
            registry.addDevice(1L, SESSION_KEY, "host", "host-hash", true, 2);
            registry.addDevice(1L, SESSION_KEY, "a", "a-hash", false, 2);

            assertThatThrownBy(() -> registry.addDevice(1L, SESSION_KEY, "b", "b-hash", false, 2))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_DEVICE_LIMIT_EXCEEDED.getMessage());
        }
    }

    @Nested
    class Collect {

        @BeforeEach
        void join() {
            registry.addDevice(1L, SESSION_KEY, "host", "host-hash", true, 8);
            registry.addDevice(1L, SESSION_KEY, "audience", "audience-hash", false, 8);
        }

        @Test
        void 참가_기기가_모두_올리면_바로_닫아_넘긴다() {
            AudioCandidateV2 host = candidate("host");
            AudioCandidateV2 audience = candidate("audience");
            registry.addCandidate(SESSION_KEY, "0-1", host);
            registry.addCandidate(SESSION_KEY, "0-1", audience);

            List<ClosedTurnV2> closed = registry.drainClosable(WINDOW);

            assertThat(closed).containsExactly(new ClosedTurnV2(1L, SESSION_KEY, "0-1", List.of(host, audience)));
        }

        @Test
        void 일부만_올리면_창_시간이_지난_뒤에_넘긴다() {
            registry.addCandidate(SESSION_KEY, "0-1", candidate("host"));

            assertThat(registry.drainClosable(WINDOW)).isEmpty();
            assertThat(registry.isSettled(SESSION_KEY)).isFalse();

            clock.advance(WINDOW);
            assertThat(registry.drainClosable(WINDOW)).hasSize(1);
        }

        @Test
        void 넘긴_턴이_DB에_기록되기_전까지는_정리되지_않은_것으로_본다() {
            registry.addCandidate(SESSION_KEY, "0-1", candidate("host"));
            registry.addCandidate(SESSION_KEY, "0-1", candidate("audience"));
            registry.drainClosable(WINDOW);

            assertThat(registry.isSettled(SESSION_KEY)).isFalse();
            registry.markPersisted(SESSION_KEY);
            assertThat(registry.isSettled(SESSION_KEY)).isTrue();
        }

        @Test
        void 닫힌_턴에는_후보를_더_받지_않는다() {
            registry.addCandidate(SESSION_KEY, "0-1", candidate("host"));
            registry.addCandidate(SESSION_KEY, "0-1", candidate("audience"));
            registry.drainClosable(WINDOW);

            assertThatThrownBy(() -> registry.addCandidate(SESSION_KEY, "0-1", candidate("host")))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_TURN_CLOSED.getMessage());
        }

        @Test
        void 상태가_없는_세션이면_정리된_것으로_본다() {
            assertThat(registry.isSettled("missing")).isTrue();
        }
    }

    @Nested
    class Remove {

        @Test
        void 오래_활동이_없는_세션을_지우고_남은_후보를_돌려준다() {
            registry.addDevice(1L, SESSION_KEY, "host", "host-hash", true, 8);
            registry.addDevice(1L, SESSION_KEY, "audience", "audience-hash", false, 8);
            AudioCandidateV2 pending = candidate("host");
            registry.addCandidate(SESSION_KEY, "0-1", pending);

            assertThat(registry.removeIdle(Duration.ofHours(1))).isEmpty();
            clock.advance(Duration.ofHours(2));

            assertThat(registry.removeIdle(Duration.ofHours(1))).containsExactly(pending);
            assertThat(registry.findDevice(SESSION_KEY, "host-hash")).isEmpty();
        }
    }

    private AudioCandidateV2 candidate(String deviceId) {
        return new AudioCandidateV2(deviceId, false, Path.of(deviceId), "audio/webm", null, 40_000, 10_000, 0, 0,
                null, AudioQualityMetricsV2.empty(), clock.instant());
    }

    private static class MutableClock extends Clock {

        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        private void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
