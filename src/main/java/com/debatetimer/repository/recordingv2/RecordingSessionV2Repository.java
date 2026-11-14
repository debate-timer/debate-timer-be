package com.debatetimer.repository.recordingv2;

import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.entity.recordingv2.RecordingSessionV2Entity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RecordingSessionV2Repository extends Repository<RecordingSessionV2Entity, Long> {

    RecordingSessionV2Entity save(RecordingSessionV2Entity entity);

    Optional<RecordingSessionV2Entity> findById(long id);

    Optional<RecordingSessionV2Entity> findBySessionKey(String sessionKey);

    List<RecordingSessionV2Entity> findAllByStatus(RecordingSessionStatusV2 status);

    default RecordingSessionV2Entity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_NOT_FOUND));
    }

    default RecordingSessionV2Entity getBySessionKey(String sessionKey) {
        return findBySessionKey(sessionKey)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.RECORDING_SESSION_NOT_FOUND));
    }
}
