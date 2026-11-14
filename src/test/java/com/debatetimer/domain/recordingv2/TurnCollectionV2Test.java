package com.debatetimer.domain.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TurnCollectionV2Test {

    private static final Instant OPENED_AT = Instant.parse("2026-11-12T00:00:00Z");
    private static final Duration WINDOW = Duration.ofSeconds(20);

    @Test
    void 참가_기기가_모두_올리면_창_시간_전이라도_닫는다() {
        TurnCollectionV2 collection = new TurnCollectionV2("0-1", OPENED_AT);
        collection.add(candidate("host"));
        collection.add(candidate("audience"));

        assertThat(collection.shouldClose(Set.of("host", "audience"), OPENED_AT.plusSeconds(1), WINDOW)).isTrue();
    }

    @Test
    void 올리지_않은_기기가_있으면_창_시간까지_기다린다() {
        TurnCollectionV2 collection = new TurnCollectionV2("0-1", OPENED_AT);
        collection.add(candidate("host"));

        assertThat(collection.shouldClose(Set.of("host", "audience"), OPENED_AT.plusSeconds(19), WINDOW)).isFalse();
        assertThat(collection.shouldClose(Set.of("host", "audience"), OPENED_AT.plusSeconds(20), WINDOW)).isTrue();
    }

    @Test
    void 같은_기기가_다시_올리면_새_후보로_바꾸고_이전_후보를_돌려준다() {
        TurnCollectionV2 collection = new TurnCollectionV2("0-1", OPENED_AT);
        AudioCandidateV2 first = candidate("host");
        AudioCandidateV2 second = candidate("host");

        assertThat(collection.add(first)).isEmpty();
        assertThat(collection.add(second)).contains(first);
        assertThat(collection.close()).containsExactly(second);
    }

    @Test
    void 닫힌_턴은_후보를_받지_않고_다시_닫지_않는다() {
        TurnCollectionV2 collection = new TurnCollectionV2("0-1", OPENED_AT);
        collection.add(candidate("host"));
        collection.close();

        assertThat(collection.isClosed()).isTrue();
        assertThat(collection.shouldClose(Set.of(), OPENED_AT.plusSeconds(60), WINDOW)).isFalse();
        assertThatThrownBy(() -> collection.add(candidate("audience")))
                .isInstanceOf(IllegalStateException.class);
    }

    private AudioCandidateV2 candidate(String deviceId) {
        return new AudioCandidateV2(deviceId, false, Path.of(deviceId), "audio/webm", null, 40_000, 10_000, 0, 0,
                null, AudioQualityMetricsV2.empty(), OPENED_AT);
    }
}
