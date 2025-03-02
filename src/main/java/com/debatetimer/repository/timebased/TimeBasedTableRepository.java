package com.debatetimer.repository.timebased;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface TimeBasedTableRepository extends Repository<TimeBasedTable, Long> {

    TimeBasedTable save(TimeBasedTable timeBasedTable);

    Optional<TimeBasedTable> findById(long id);

    default TimeBasedTable getById(long tableId) {
        return findById(tableId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    List<TimeBasedTable> findAllByMember(Member member);

    void delete(TimeBasedTable table);
}
