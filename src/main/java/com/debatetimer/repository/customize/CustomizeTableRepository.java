package com.debatetimer.repository.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface CustomizeTableRepository extends Repository<CustomizeTable, Long> {

    CustomizeTable save(CustomizeTable customizeTable);

    Optional<CustomizeTable> findById(long id);

    default CustomizeTable getById(long tableId) {
        return findById(tableId)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    List<CustomizeTable> findAllByMember(Member member);

    void delete(CustomizeTable table);
}
