package com.debatetimer.domain.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AudioQualityMetricsV2Test {

    @Test
    void 모든_값이_실제_녹음_범위_안이면_믿을_만한_지표다() {
        assertThat(new AudioQualityMetricsV2(-20.0, -3.0, 0.0, 0.3, 25.0).isPlausible()).isTrue();
    }

    @Test
    void 값이_하나라도_빠지면_지표가_없는_것으로_본다() {
        AudioQualityMetricsV2 metrics = new AudioQualityMetricsV2(-20.0, -3.0, 0.0, null, 25.0);

        assertThat(metrics.isComplete()).isFalse();
        assertThat(metrics.isPlausible()).isFalse();
    }

    @Test
    void 음량이_0dBFS를_넘으면_믿지_않는다() {
        assertThat(new AudioQualityMetricsV2(3.0, 5.0, 0.0, 0.3, 25.0).isPlausible()).isFalse();
    }

    @Test
    void 최대_음량이_평균_음량보다_작으면_믿지_않는다() {
        assertThat(new AudioQualityMetricsV2(-10.0, -20.0, 0.0, 0.3, 25.0).isPlausible()).isFalse();
    }

    @Test
    void 비율이_0과_1_사이가_아니면_믿지_않는다() {
        assertThat(new AudioQualityMetricsV2(-20.0, -3.0, 1.5, 0.3, 25.0).isPlausible()).isFalse();
        assertThat(new AudioQualityMetricsV2(-20.0, -3.0, 0.0, -0.1, 25.0).isPlausible()).isFalse();
    }

    @Test
    void NaN은_믿지_않는다() {
        assertThat(new AudioQualityMetricsV2(Double.NaN, -3.0, 0.0, 0.3, 25.0).isPlausible()).isFalse();
    }
}
