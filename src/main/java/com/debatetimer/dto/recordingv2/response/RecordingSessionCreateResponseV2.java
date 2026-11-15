package com.debatetimer.dto.recordingv2.response;

/**
 * joinCode는 청중 기기에 공유 링크·QR로 넘기고, deviceToken은 사회자 기기가 업로드할 때 쓴다. 둘 다 다시 조회할 수 없다.
 */
public record RecordingSessionCreateResponseV2(
        String sessionKey,
        String joinCode,
        String deviceId,
        String deviceToken
) {
}
