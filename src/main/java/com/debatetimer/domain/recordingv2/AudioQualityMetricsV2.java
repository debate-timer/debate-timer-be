package com.debatetimer.domain.recordingv2;

import org.springframework.lang.Nullable;

/**
 * 기기가 Web Audio로 녹음하며 계산해 함께 보낸 음성 품질 지표. 값은 기기가 보낸 그대로라 믿지 않고 범위를 확인한 뒤 점수에 쓴다.
 * 하나라도 빠지면 지표가 없는 후보로 본다.
 */
public record AudioQualityMetricsV2(
        // 평균 음량(dBFS). 0이 최대이고 음수다.
        @Nullable Double rmsDb,
        // 최대 순간 음량(dBFS)
        @Nullable Double peakDb,
        // 최대 진폭에 닿은 샘플 비율(0~1)
        @Nullable Double clippingRatio,
        // 무음으로 본 구간 비율(0~1)
        @Nullable Double silenceRatio,
        // 발화 구간 음량과 잡음 바닥의 차이(dB)
        @Nullable Double snrDb
) {

    private static final double MIN_DB = -120;
    private static final double MIN_SNR_DB = -20;
    private static final double MAX_SNR_DB = 100;

    public static AudioQualityMetricsV2 empty() {
        return new AudioQualityMetricsV2(null, null, null, null, null);
    }

    public boolean isComplete() {
        return rmsDb != null && peakDb != null && clippingRatio != null && silenceRatio != null && snrDb != null;
    }

    // 실제 녹음에서 나올 수 없는 값이면 조작했거나 계산이 잘못된 지표로 본다.
    public boolean isPlausible() {
        return isComplete()
                && isBetween(rmsDb, MIN_DB, 0)
                && isBetween(peakDb, MIN_DB, 0)
                && peakDb >= rmsDb
                && isBetween(clippingRatio, 0, 1)
                && isBetween(silenceRatio, 0, 1)
                && isBetween(snrDb, MIN_SNR_DB, MAX_SNR_DB);
    }

    private boolean isBetween(double value, double min, double max) {
        return !Double.isNaN(value) && value >= min && value <= max;
    }
}
