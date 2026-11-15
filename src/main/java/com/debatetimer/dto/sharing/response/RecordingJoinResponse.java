package com.debatetimer.dto.sharing.response;

import com.debatetimer.dto.sharing.request.RecordingJoinRequest;

public record RecordingJoinResponse(
        String sessionKey,
        String joinCode
) {

    public RecordingJoinResponse(RecordingJoinRequest request) {
        this(request.sessionKey(), request.joinCode());
    }
}
