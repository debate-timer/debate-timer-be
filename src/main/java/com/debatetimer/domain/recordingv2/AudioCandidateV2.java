package com.debatetimer.domain.recordingv2;

import com.debatetimer.domain.customize.Stance;
import java.nio.file.Path;
import java.time.Instant;
import org.springframework.lang.Nullable;

/**
 * 한 턴에 한 기기가 올린 녹음 후보. 오디오는 힙에 두지 않고 임시 파일로 받아 두고 경로만 들고 있는다.
 */
public record AudioCandidateV2(
        String deviceId,
        // 녹음 세션을 연 사회자 기기면 true. 점수가 같으면 사회자 기기를 고른다.
        boolean host,
        Path file,
        @Nullable String contentType,
        @Nullable String originalFilename,
        long sizeBytes,
        long durationMs,
        // 녹음을 시작한 시각(서버 시계 기준 epoch ms). 기기는 공유 이벤트의 serverTime으로 서버 시계를 추정해 보낸다.
        long startedAtEpochMs,
        int timeBoxIndex,
        @Nullable Stance team,
        AudioQualityMetricsV2 metrics,
        Instant receivedAt
) {

    // 압축 오디오(webm/opus 등)로 볼 수 있는 비트레이트 범위(bps). 벗어나면 길이를 속였거나 깨진 파일로 본다.
    static final long MIN_BITRATE = 4_000;
    static final long MAX_BITRATE = 512_000;

    public boolean isValid() {
        if (sizeBytes <= 0 || durationMs <= 0) {
            return false;
        }
        long bitrate = sizeBytes * 8 * 1_000 / durationMs;
        return bitrate >= MIN_BITRATE && bitrate <= MAX_BITRATE;
    }

    public boolean hasPlausibleMetrics() {
        return metrics.isPlausible();
    }
}
