package com.debatetimer.domain.recordingv2;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 한 턴의 녹음 후보를 모으는 창. 첫 후보가 도착한 때 열리고, 참가 기기가 모두 올리거나 창 시간이 지나면 닫는다.
 * 닫힌 뒤 도착한 후보는 받지 않는다. 동시 접근은 호출하는 쪽에서 막는다.
 */
public class TurnCollectionV2 {

    private final String turnKey;
    private final Instant openedAt;
    private final Map<String, AudioCandidateV2> candidates = new LinkedHashMap<>();
    private boolean closed;

    public TurnCollectionV2(String turnKey, Instant openedAt) {
        this.turnKey = turnKey;
        this.openedAt = openedAt;
    }

    /**
     * 후보를 받는다. 같은 기기가 다시 올리면 새 후보로 바꾸고, 호출하는 쪽이 파일을 지울 수 있도록 이전 후보를 돌려준다.
     */
    public Optional<AudioCandidateV2> add(AudioCandidateV2 candidate) {
        if (closed) {
            throw new IllegalStateException("이미 닫힌 턴입니다. turnKey=" + turnKey);
        }
        return Optional.ofNullable(candidates.put(candidate.deviceId(), candidate));
    }

    public boolean shouldClose(Set<String> participantDeviceIds, Instant now, Duration window) {
        if (closed) {
            return false;
        }
        return candidates.keySet().containsAll(participantDeviceIds) || !now.isBefore(openedAt.plus(window));
    }

    public List<AudioCandidateV2> close() {
        closed = true;
        return new ArrayList<>(candidates.values());
    }

    public boolean isClosed() {
        return closed;
    }

    public String getTurnKey() {
        return turnKey;
    }
}
