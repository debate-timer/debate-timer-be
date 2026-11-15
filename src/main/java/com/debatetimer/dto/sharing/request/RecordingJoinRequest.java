package com.debatetimer.dto.sharing.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 다기기 녹음 참가 정보. 청중 기기가 이 값으로 녹음 세션에 참가한다.
 */
public record RecordingJoinRequest(
        @NotBlank
        String sessionKey,

        @NotBlank
        String joinCode
) {
}
