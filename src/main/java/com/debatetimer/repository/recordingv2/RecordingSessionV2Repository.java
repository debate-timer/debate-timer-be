package com.debatetimer.repository.recordingv2;

import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.entity.recordingv2.RecordingSessionV2Entity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RecordingSessionV2Repository extends Repository<RecordingSessionV2Entity, Long> {

    RecordingSessionV2Entity save(RecordingSessionV2Entity entity);

    Optional<RecordingSessionV2Entity> findById(long id);

    Optional<RecordingSessionV2Entity> findBySessionKey(String sessionKey);

    List<RecordingSessionV2Entity> findAllByStatus(RecordingSessionStatusV2 status);

    List<RecordingSessionV2Entity> findAllByStatusAndCreatedAtBefore(RecordingSessionStatusV2 status,
                                                                     LocalDateTime createdAt);

    // 녹음 중인 세션만 바꿔, 종료 요청이 겹쳐도 이미 마무리 중이거나 끝난 세션을 되돌리지 않는다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE RecordingSessionV2Entity s
            SET s.status = :finishing, s.finishedAt = :finishedAt
            WHERE s.id = :id AND s.status = :recording
            """)
    int updateStatusIfRecording(
            @Param("id") long id,
            @Param("finishedAt") LocalDateTime finishedAt,
            @Param("finishing") RecordingSessionStatusV2 finishing,
            @Param("recording") RecordingSessionStatusV2 recording
    );

    default RecordingSessionV2Entity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_NOT_FOUND));
    }

    default RecordingSessionV2Entity getBySessionKey(String sessionKey) {
        return findBySessionKey(sessionKey)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_NOT_FOUND));
    }
}
