package com.debatetimer.repository.transcript;

import com.debatetimer.domain.transcript.TranscriptSummary;
import com.debatetimer.entity.transcript.TranscriptEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TranscriptRepository extends Repository<TranscriptEntity, Long> {

    TranscriptEntity save(TranscriptEntity transcriptEntity);

    Optional<TranscriptEntity> findById(long id);

    Optional<TranscriptEntity> findByIdAndMemberId(long id, long memberId);

    // 속기록·보고서 본문(LONGTEXT)은 읽지 않고 목록에 필요한 값만 가져온다.
    @Query("""
            SELECT new com.debatetimer.domain.transcript.TranscriptSummary(
                t.id, t.tableId, t.sttMode, t.status, t.refining, t.createdAt)
            FROM TranscriptEntity t
            WHERE t.memberId = :memberId
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<TranscriptSummary> findSummariesByMemberId(long memberId);

    default TranscriptEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TRANSCRIPT_NOT_FOUND));
    }

    default TranscriptEntity getByIdAndMemberId(long id, long memberId) {
        return findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TRANSCRIPT_NOT_FOUND));
    }
}
