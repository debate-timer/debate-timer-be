package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParliamentaryTableRepository extends JpaRepository<ParliamentaryTable, Long> {

    List<ParliamentaryTable> findAllByMember(Member member);

    Optional<ParliamentaryTable> findByIdAndMemberId(long id, long memberId);
}
