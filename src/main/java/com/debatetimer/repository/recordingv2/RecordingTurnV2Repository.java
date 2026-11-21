package com.debatetimer.repository.recordingv2;

import com.debatetimer.entity.recordingv2.RecordingTurnV2Entity;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;

public interface RecordingTurnV2Repository extends Repository<RecordingTurnV2Entity, Long> {

    RecordingTurnV2Entity save(RecordingTurnV2Entity entity);

    Optional<RecordingTurnV2Entity> findById(long id);

    List<RecordingTurnV2Entity> findAllBySessionIdOrderByIdAsc(long sessionId);

    boolean existsBySessionIdAndTurnKey(long sessionId, String turnKey);

    Optional<RecordingTurnV2Entity> findBySessionIdAndTurnKey(long sessionId, String turnKey);

    // 실패로 바꾸는 동안 처리 중인 앱이 완료를 저장하면 덮어쓰지 않도록, 바꿀 턴을 잠그고 읽는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RecordingTurnV2Entity> findAllByStatusAndCreatedAtBefore(RecordingTurnStatusV2 status,
                                                                  LocalDateTime createdAt);
}
