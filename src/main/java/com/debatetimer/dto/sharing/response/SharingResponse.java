package com.debatetimer.dto.sharing.response;

import com.debatetimer.domain.sharing.TimerEventType;
import jakarta.annotation.Nullable;

public record SharingResponse(
        TimerEventType eventType,

        @Nullable
        Long version,

        @Nullable
        Long serverTime,

        @Nullable
        TimerEventDataResponse data,

        // 녹음 참여 이벤트(RECORDING_OPENED)의 다기기 녹음 참가 정보
        @Nullable
        RecordingJoinResponse recording
) {

    public SharingResponse(TimerEventType eventType) {
        this(eventType, null, null, null, null);
    }

    public SharingResponse(TimerEventType eventType, @Nullable Long version, @Nullable Long serverTime,
                           @Nullable TimerEventDataResponse data) {
        this(eventType, version, serverTime, data, null);
    }
}
