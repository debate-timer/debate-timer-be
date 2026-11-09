package com.debatetimer.repository.transcript;

import com.debatetimer.domain.transcript.TranscriptSummary;
import com.debatetimer.entity.transcript.TranscriptEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import com.debatetimer.domain.transcript.TranscriptOverview;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TranscriptRepository extends Repository<TranscriptEntity, Long> {

    TranscriptEntity save(TranscriptEntity transcriptEntity);

    Optional<TranscriptEntity> findById(long id);

    Optional<TranscriptEntity> findByIdAndMemberId(long id, long memberId);

    // 속기록·보고서 본문(LONGTEXT)은 읽지 않고 목록에 필요한 값만 가져온다.
    @Query("""
            SELECT new com.debatetimer.domain.transcript.TranscriptSummary(
                t.id, t.tableId, t.tableName, t.agenda, t.sttMode, t.status, t.refining, t.createdAt)
            FROM TranscriptEntity t
            WHERE t.memberId = :memberId
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<TranscriptSummary> findSummariesByMemberId(long memberId);

    // 어드민 목록. 본문(LONGTEXT)은 읽지 않고, 탈퇴 등으로 회원이 없으면 이메일은 null 이다.
    @Query(value = """
            SELECT new com.debatetimer.domain.transcript.TranscriptOverview(
                t.id, t.memberId, m.email, t.tableId, t.tableName, t.agenda, t.sttMode, t.status, t.refining,
                t.sourceTranscriptId, t.createdAt)
            FROM TranscriptEntity t
            LEFT JOIN Member m ON m.id = t.memberId
            ORDER BY t.id DESC
            """,
            countQuery = "SELECT COUNT(t) FROM TranscriptEntity t")
    Page<TranscriptOverview> findOverviews(Pageable pageable);

    default TranscriptEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TRANSCRIPT_NOT_FOUND));
    }

    default TranscriptEntity getByIdAndMemberId(long id, long memberId) {
        return findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TRANSCRIPT_NOT_FOUND));
    }
}
