package com.debatetimer.repository.member;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

    Optional<Member> findById(long id);

    Member save(Member member);

    default Member getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.MEMBER_NOT_FOUND));
    }
}
