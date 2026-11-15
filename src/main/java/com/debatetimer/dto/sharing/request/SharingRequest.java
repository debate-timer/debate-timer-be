package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

public record SharingRequest(
        @NotNull
        TimerEventType eventType,

        @Nullable
        Long version,

        @Valid
        @Nullable
        TimerEventInfoRequest data,

        // 녹음 참여 이벤트(RECORDING_OPENED)의 다기기 녹음 참가 정보
        @Valid
        @Nullable
        RecordingJoinRequest recording
) {

    public SharingRequest(TimerEventType eventType, @Nullable Long version, @Nullable TimerEventInfoRequest data) {
        this(eventType, version, data, null);
    }

    public TimerEvent toTimerEvent() {
        if (eventType.requiresVersion() && version == null) {
            throw new DTClientErrorException(ClientErrorCode.VERSION_REQUIRED_TIMER_EVENT);
        }
        if (eventType.requiresRecording() && recording == null) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIMER_EVENT);
        }
        return Optional.ofNullable(data)
                .map(TimerEventInfoRequest::toTimerEventInfo)
                .map(eventData -> new TimerEvent(eventType, eventData))
                .orElseGet(() -> new TimerEvent(eventType));
    }
}
