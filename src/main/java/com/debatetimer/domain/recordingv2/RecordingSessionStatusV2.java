package com.debatetimer.domain.recordingv2;

public enum RecordingSessionStatusV2 {

    // 기기들이 턴 녹음을 올리는 중
    RECORDING,
    // 사회자가 토론을 끝내 남은 턴 전사를 기다린 뒤 속기록을 만드는 중
    FINISHING,
    // 속기록을 만들었다
    COMPLETED,
    // 전사된 발언이 없거나 속기록을 만들지 못했다
    FAILED,
    ;

    public boolean isRecording() {
        return this == RECORDING;
    }
}
