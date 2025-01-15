package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ParliamentaryTableRepository extends Repository<ParliamentaryTable, Long> {

    ParliamentaryTable save(ParliamentaryTable parliamentaryTable);

    Optional<ParliamentaryTable> findById(long id);

    default ParliamentaryTable getById(long tableId) {
        return findById(tableId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    List<ParliamentaryTable> findAllByMember(Member member);

    void delete(ParliamentaryTable table);
}
