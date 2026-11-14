package com.debatetimer.repository.recordingv2;

import com.debatetimer.entity.recordingv2.RecordingTurnV2Entity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RecordingTurnV2Repository extends Repository<RecordingTurnV2Entity, Long> {

    RecordingTurnV2Entity save(RecordingTurnV2Entity entity);

    Optional<RecordingTurnV2Entity> findById(long id);

    List<RecordingTurnV2Entity> findAllBySessionIdOrderByIdAsc(long sessionId);

    boolean existsBySessionIdAndTurnKey(long sessionId, String turnKey);

    default RecordingTurnV2Entity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalStateException("녹음 턴을 찾을 수 없습니다. id=" + id));
    }
}
