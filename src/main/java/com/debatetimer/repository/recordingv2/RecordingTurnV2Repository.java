package com.debatetimer.repository.recordingv2;

import com.debatetimer.entity.recordingv2.RecordingTurnV2Entity;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RecordingTurnV2Repository extends Repository<RecordingTurnV2Entity, Long> {

    RecordingTurnV2Entity save(RecordingTurnV2Entity entity);

    Optional<RecordingTurnV2Entity> findById(long id);

    List<RecordingTurnV2Entity> findAllBySessionIdOrderByIdAsc(long sessionId);

    boolean existsBySessionIdAndTurnKey(long sessionId, String turnKey);

    List<RecordingTurnV2Entity> findAllByStatusAndCreatedAtBefore(RecordingTurnStatusV2 status,
                                                                  LocalDateTime createdAt);
}
