package com.debatetimer.repository.customize;

import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface CustomizeTableRepository extends Repository<CustomizeTableEntity, Long> {

    CustomizeTableEntity save(CustomizeTableEntity customizeTableEntity);

    Optional<CustomizeTableEntity> findById(long id);

    List<CustomizeTableEntity> findAllByMember(Member member);

    Optional<CustomizeTableEntity> findByIdAndMember(long tableId, Member member);

    default CustomizeTableEntity getByIdAndMember(long tableId, Member member) {
        return findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    default CustomizeTableEntity getById(long tableId) {
        return findById(tableId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    // 트랜잭션 밖에서도 테이블 소유 회원 정보를 쓸 수 있도록 함께 조회한다
    @EntityGraph(attributePaths = "member")
    Optional<CustomizeTableEntity> findWithMemberById(long id);

    default CustomizeTableEntity getWithMemberById(long tableId) {
        return findWithMemberById(tableId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    @EntityGraph(attributePaths = "member")
    Page<CustomizeTableEntity> findAllBy(Pageable pageable);

    @Query("SELECT t.createdAt FROM CustomizeTableEntity t WHERE t.createdAt >= :from AND t.createdAt < :to")
    List<LocalDateTime> findCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT t.usedAt FROM CustomizeTableEntity t WHERE t.usedAt >= :from AND t.usedAt < :to")
    List<LocalDateTime> findUsedAtBetween(LocalDateTime from, LocalDateTime to);

    void delete(CustomizeTableEntity table);
}
