package com.debatetimer.repository.member;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    Optional<Member> findById(long id);

    default Member getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.MEMBER_NOT_FOUND));
    }

    Optional<Member> findByEmail(String email);

    default Member getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.MEMBER_NOT_FOUND));
    }

    @Query("SELECT m.createdAt FROM Member m WHERE m.createdAt >= :from AND m.createdAt < :to")
    List<LocalDateTime> findCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByCreatedAtBefore(LocalDateTime createdAt);
}
