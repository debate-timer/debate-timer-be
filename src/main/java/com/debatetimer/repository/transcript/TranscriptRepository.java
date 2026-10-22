package com.debatetimer.repository.transcript;

import com.debatetimer.entity.transcript.TranscriptEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface TranscriptRepository extends Repository<TranscriptEntity, Long> {

    TranscriptEntity save(TranscriptEntity transcriptEntity);

    Optional<TranscriptEntity> findById(long id);

    Optional<TranscriptEntity> findByIdAndMemberId(long id, long memberId);

    default TranscriptEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TRANSCRIPT_NOT_FOUND));
    }

    default TranscriptEntity getByIdAndMemberId(long id, long memberId) {
        return findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TRANSCRIPT_NOT_FOUND));
    }
}
