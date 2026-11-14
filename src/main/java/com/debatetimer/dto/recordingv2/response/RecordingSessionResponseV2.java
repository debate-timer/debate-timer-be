package com.debatetimer.dto.recordingv2.response;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 사회자가 폴링하는 녹음 세션 상태. COMPLETED가 되면 transcriptId로 기존 속기록 API를 조회한다.
 */
public record RecordingSessionResponseV2(
        String sessionKey,
        RecordingSessionStatusV2 status,
        @Nullable Long transcriptId,
        @Nullable String errorMessage,
        List<TurnResponse> turns
) {

    public RecordingSessionResponseV2(RecordingSessionV2 session, List<RecordingTurnV2> turns) {
        this(session.sessionKey(), session.status(), session.transcriptId(), session.errorMessage(),
                turns.stream().map(TurnResponse::new).toList());
    }

    public record TurnResponse(
            String turnKey,
            int timeBoxIndex,
            @Nullable Stance team,
            RecordingTurnStatusV2 status,
            int candidateCount,
            @Nullable String selectedDeviceId,
            @Nullable String errorMessage
    ) {

        private TurnResponse(RecordingTurnV2 turn) {
            this(turn.turnKey(), turn.timeBoxIndex(), turn.team(), turn.status(), turn.candidateCount(),
                    turn.selectedDeviceId(), turn.errorMessage());
        }
    }
}
