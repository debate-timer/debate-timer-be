package com.debatetimer.dto.sharing.request;

import com.debatetimer.domain.sharing.ChairmanNoticeType;
import jakarta.annotation.Nullable;

public record ChairmanSharingRequest(
        ChairmanNoticeType type,

        long roomId,

        @Nullable
        String activeSessionId
) {

    public static ChairmanSharingRequest syncRequest(long roomId) {
        return new ChairmanSharingRequest(ChairmanNoticeType.SYNC_REQUEST, roomId, null);
    }

    public static ChairmanSharingRequest replaced(long roomId, String activeSessionId) {
        return new ChairmanSharingRequest(ChairmanNoticeType.REPLACED, roomId, activeSessionId);
    }
}
