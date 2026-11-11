package com.debatetimer.domain.recordingv2;

import java.util.Comparator;
import java.util.List;

/**
 * 한 턴에 모인 녹음 후보를 음성 품질 순으로 줄 세운다. 전사는 맨 앞 후보 하나만 하고, 실패하면 다음 후보를 쓴다.
 * - 크기·길이가 맞지 않는 후보는 뺀다.
 * - 믿을 만한 지표가 있는 후보를 지표가 없거나 이상한 후보보다 앞에 둔다.
 * - 점수는 SNR, 적정 음량, 발화 비율, 클리핑, 녹음 길이(같은 턴에서 가장 긴 녹음 대비)로 매긴다.
 * - 점수가 같으면 사회자 기기, 먼저 도착한 후보 순이다.
 */
public class AudioQualityScorerV2 {

    static final double SNR_WEIGHT = 40;
    static final double MAX_SNR_DB = 40;
    static final double LOUDNESS_WEIGHT = 20;
    static final double MIN_GOOD_RMS_DB = -30;
    static final double MAX_GOOD_RMS_DB = -12;
    static final double SPEECH_WEIGHT = 10;
    static final double CLIPPING_PENALTY_PER_RATIO = 200;
    static final double MAX_CLIPPING_PENALTY = 30;
    static final double COMPLETENESS_WEIGHT = 30;

    public List<AudioCandidateV2> rank(List<AudioCandidateV2> candidates) {
        List<AudioCandidateV2> valid = candidates.stream()
                .filter(AudioCandidateV2::isValid)
                .toList();
        long longestDurationMs = valid.stream()
                .mapToLong(AudioCandidateV2::durationMs)
                .max()
                .orElse(0);

        Comparator<AudioCandidateV2> order = Comparator
                .comparing((AudioCandidateV2 candidate) -> !candidate.hasPlausibleMetrics())
                .thenComparing(candidate -> score(candidate, longestDurationMs), Comparator.reverseOrder())
                .thenComparing(candidate -> !candidate.host())
                .thenComparing(AudioCandidateV2::receivedAt);
        return valid.stream()
                .sorted(order)
                .toList();
    }

    double score(AudioCandidateV2 candidate, long longestDurationMs) {
        double completeness = COMPLETENESS_WEIGHT * candidate.durationMs() / longestDurationMs;
        if (!candidate.hasPlausibleMetrics()) {
            return completeness;
        }
        AudioQualityMetricsV2 metrics = candidate.metrics();
        return completeness
                + snrScore(metrics.snrDb())
                + loudnessScore(metrics.rmsDb())
                + SPEECH_WEIGHT * (1 - metrics.silenceRatio())
                - Math.min(MAX_CLIPPING_PENALTY, CLIPPING_PENALTY_PER_RATIO * metrics.clippingRatio());
    }

    private double snrScore(double snrDb) {
        double clamped = Math.max(0, Math.min(MAX_SNR_DB, snrDb));
        return SNR_WEIGHT * clamped / MAX_SNR_DB;
    }

    // 적정 범위 안이면 만점이고, 벗어난 1dB마다 1점씩 깎는다.
    private double loudnessScore(double rmsDb) {
        if (rmsDb < MIN_GOOD_RMS_DB) {
            return Math.max(0, LOUDNESS_WEIGHT - (MIN_GOOD_RMS_DB - rmsDb));
        }
        if (rmsDb > MAX_GOOD_RMS_DB) {
            return Math.max(0, LOUDNESS_WEIGHT - (rmsDb - MAX_GOOD_RMS_DB));
        }
        return LOUDNESS_WEIGHT;
    }
}
