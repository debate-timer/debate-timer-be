package com.debatetimer.repository.parliamentary_debate;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary_debate.ParliamentaryTable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParliamentaryTableRepository extends JpaRepository<ParliamentaryTable, Long> {

    List<ParliamentaryTable> findAllByMember(Member member);
}
