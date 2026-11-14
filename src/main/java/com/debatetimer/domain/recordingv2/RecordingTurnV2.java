package com.debatetimer.domain.recordingv2;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.transcript.Utterance;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 수집 창이 닫힌 턴 하나. 고른 후보의 전사 결과(offsetMs는 턴 녹음 시작 기준)를 보관한다.
 */
public record RecordingTurnV2(
        long id,
        long sessionId,
        String turnKey,
        int timeBoxIndex,
        @Nullable Stance team,
        long startedAtEpochMs,
        RecordingTurnStatusV2 status,
        int candidateCount,
        @Nullable String selectedDeviceId,
        @Nullable String s3Key,
        List<Utterance> utterances,
        @Nullable String errorMessage
) {
}
