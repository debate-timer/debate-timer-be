package com.debatetimer.domain.recordingv2;

import java.util.List;

/**
 * 수집 창이 닫혀 고르고 전사할 차례가 된 턴.
 */
public record ClosedTurnV2(long sessionId, String sessionKey, String turnKey, List<AudioCandidateV2> candidates) {
}
