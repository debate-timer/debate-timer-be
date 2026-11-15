package com.debatetimer.dto.recordingv2.request;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.recordingv2.AudioQualityMetricsV2;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.lang.Nullable;

/**
 * 턴 녹음 업로드의 multipart 폼 값. 품질 지표는 기기가 계산하지 못했으면 비워 보낸다.
 */
public record RecordingTurnUploadRequestV2(
        @NotNull @PositiveOrZero Integer timeBoxIndex,
        // 자유토론처럼 한 타임박스 안에서 발언 팀이 바뀌는 경우에만 채운다.
        @Nullable Stance team,
        @NotNull @Positive Long durationMs,
        // 녹음을 시작한 시각(서버 시계 기준 epoch ms)
        @NotNull @PositiveOrZero Long startedAtEpochMs,
        @Nullable Double rmsDb,
        @Nullable Double peakDb,
        @Nullable Double clippingRatio,
        @Nullable Double silenceRatio,
        @Nullable Double snrDb
) {

    public AudioQualityMetricsV2 toMetrics() {
        return new AudioQualityMetricsV2(rmsDb, peakDb, clippingRatio, silenceRatio, snrDb);
    }
}
