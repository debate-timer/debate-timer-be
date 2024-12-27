package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParliamentaryTableRepository extends JpaRepository<ParliamentaryTable, Long> {

    List<ParliamentaryTable> findAllByMember(Member member);

    default ParliamentaryTable getById(Long id) {
        //TODO ClientError로 수정
        return findById(id).orElseThrow(() -> new IllegalArgumentException("해당 테이블이 존재하지 않습니다"));
    }

    Optional<ParliamentaryTable> findByIdAndMemberId(long id, long memberId);
}
