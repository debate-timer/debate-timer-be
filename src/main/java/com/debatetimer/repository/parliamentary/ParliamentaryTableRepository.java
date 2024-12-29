package com.debatetimer.repository.parliamentary;

import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.controller.exception.errorcode.ClientErrorCode;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParliamentaryTableRepository extends JpaRepository<ParliamentaryTable, Long> {

    List<ParliamentaryTable> findAllByMember(Member member);

    default ParliamentaryTable getOwnerTable(long tableId, long memberId) {
        return findByIdAndMemberId(tableId, memberId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.MEMBER_TABLE_NOT_FOUND));
    }

    Optional<ParliamentaryTable> findByIdAndMemberId(long id, long memberId);
}
