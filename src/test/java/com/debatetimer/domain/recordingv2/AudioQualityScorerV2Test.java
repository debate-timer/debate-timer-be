package com.debatetimer.domain.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AudioQualityScorerV2Test {

    private static final Instant NOW = Instant.parse("2026-11-12T00:00:00Z");
    // 32kbps 압축 오디오 기준 크기
    private static final long BYTES_PER_SECOND = 4_000;

    private final AudioQualityScorerV2 scorer = new AudioQualityScorerV2();

    @Nested
    class Exclusion {

        @Test
        void 크기나_길이가_0인_후보는_뺀다() {
            AudioCandidateV2 empty = candidate("empty", false, 0, 10_000, goodMetrics(), 0);
            AudioCandidateV2 noDuration = candidate("no-duration", false, 40_000, 0, goodMetrics(), 0);
            AudioCandidateV2 good = candidate("good", false, 40_000, 10_000, goodMetrics(), 0);

            assertThat(scorer.rank(List.of(empty, noDuration, good))).containsExactly(good);
        }

        @Test
        void 크기에_비해_길이가_맞지_않는_후보는_뺀다() {
            AudioCandidateV2 tooSmall = candidate("too-small", false, 100, 60_000, goodMetrics(), 0);
            AudioCandidateV2 tooLarge = candidate("too-large", false, 10_000_000, 1_000, goodMetrics(), 0);

            assertThat(scorer.rank(List.of(tooSmall, tooLarge))).isEmpty();
        }
    }

    @Nested
    class Ranking {

        @Test
        void SNR이_높은_후보를_앞에_둔다() {
            AudioCandidateV2 noisy = candidate("noisy", false, 40_000, 10_000, metrics(-20, -3, 0, 0.2, 10), 0);
            AudioCandidateV2 clear = candidate("clear", false, 40_000, 10_000, metrics(-20, -3, 0, 0.2, 30), 1);

            assertThat(scorer.rank(List.of(noisy, clear))).containsExactly(clear, noisy);
        }

        @Test
        void 클리핑이_심한_후보는_뒤로_간다() {
            AudioCandidateV2 clipped = candidate("clipped", false, 40_000, 10_000, metrics(-15, 0, 0.1, 0.2, 30), 0);
            AudioCandidateV2 clean = candidate("clean", false, 40_000, 10_000, metrics(-20, -3, 0, 0.2, 25), 1);

            assertThat(scorer.rank(List.of(clipped, clean))).containsExactly(clean, clipped);
        }

        @Test
        void 너무_작게_녹음된_후보는_뒤로_간다() {
            AudioCandidateV2 quiet = candidate("quiet", false, 40_000, 10_000, metrics(-55, -30, 0, 0.2, 30), 0);
            AudioCandidateV2 normal = candidate("normal", false, 40_000, 10_000, metrics(-20, -3, 0, 0.2, 30), 1);

            assertThat(scorer.rank(List.of(quiet, normal))).containsExactly(normal, quiet);
        }

        @Test
        void 늦게_녹음을_시작해_짧은_후보는_뒤로_간다() {
            AudioCandidateV2 partial = candidate("partial", false, 12_000, 3_000, goodMetrics(), 0);
            AudioCandidateV2 full = candidate("full", false, 40_000, 10_000, goodMetrics(), 1);

            assertThat(scorer.rank(List.of(partial, full))).containsExactly(full, partial);
        }

        @Test
        void 지표가_없거나_이상한_후보는_점수와_관계없이_지표가_있는_후보보다_뒤에_둔다() {
            AudioCandidateV2 noMetrics = candidate("no-metrics", true, 40_000, 10_000,
                    AudioQualityMetricsV2.empty(), 0);
            AudioCandidateV2 forged = candidate("forged", false, 40_000, 10_000, metrics(5, 0, 0, 0, 30), 1);
            AudioCandidateV2 poor = candidate("poor", false, 40_000, 10_000, metrics(-60, -40, 0.5, 0.9, 0), 2);

            assertThat(scorer.rank(List.of(noMetrics, forged, poor))).containsExactly(poor, noMetrics, forged);
        }

        @Test
        void 점수가_같으면_사회자_기기를_고른다() {
            AudioCandidateV2 audience = candidate("audience", false, 40_000, 10_000, goodMetrics(), 0);
            AudioCandidateV2 host = candidate("host", true, 40_000, 10_000, goodMetrics(), 1);

            assertThat(scorer.rank(List.of(audience, host))).containsExactly(host, audience);
        }

        @Test
        void 점수와_기기가_같으면_먼저_도착한_후보를_고른다() {
            AudioCandidateV2 late = candidate("late", false, 40_000, 10_000, goodMetrics(), 5);
            AudioCandidateV2 early = candidate("early", false, 40_000, 10_000, goodMetrics(), 1);

            assertThat(scorer.rank(List.of(late, early))).containsExactly(early, late);
        }
    }

    private AudioQualityMetricsV2 goodMetrics() {
        return metrics(-20, -3, 0, 0.2, 30);
    }

    private AudioQualityMetricsV2 metrics(double rmsDb, double peakDb, double clippingRatio, double silenceRatio,
                                          double snrDb) {
        return new AudioQualityMetricsV2(rmsDb, peakDb, clippingRatio, silenceRatio, snrDb);
    }

    private AudioCandidateV2 candidate(String deviceId, boolean host, long sizeBytes, long durationMs,
                                       AudioQualityMetricsV2 metrics, int receivedOrder) {
        return new AudioCandidateV2(deviceId, host, Path.of(deviceId + ".webm"), "audio/webm", deviceId + ".webm",
                sizeBytes, durationMs, 0, 0, null, metrics, NOW.plusSeconds(receivedOrder));
    }
}
