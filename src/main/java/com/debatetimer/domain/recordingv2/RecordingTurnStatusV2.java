package com.debatetimer.domain.recordingv2;

public enum RecordingTurnStatusV2 {

    // 고른 후보를 S3에 올리고 전사하는 중
    PROCESSING,
    DONE,
    // 모든 후보의 전사가 실패했다
    FAILED,
    ;

    public boolean isProcessing() {
        return this == PROCESSING;
    }
}
