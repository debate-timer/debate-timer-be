package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ParliamentaryTableRepository extends Repository<ParliamentaryTable, Long> {

    Optional<ParliamentaryTable> findById(long id);

    List<ParliamentaryTable> findAllByMember(Member member);

    ParliamentaryTable save(ParliamentaryTable parliamentaryTable);

    void delete(ParliamentaryTable table);

    default ParliamentaryTable getById(long tableId) {
        return findById(tableId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }
}
