package com.debatetimer.domain.recordingv2;

/**
 * 녹음 세션에 참가한 기기. deviceId는 S3 경로·상태 조회에 드러나는 값이고, 업로드 권한은 따로 발급한 토큰으로 확인한다.
 */
public record RecordingDeviceV2(String deviceId, boolean host) {
}
