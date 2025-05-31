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

    List<CustomizeTable> findAllByMember(Member member);

    Optional<CustomizeTable> findByIdAndMember(long tableId, Member member);

    default CustomizeTable getByIdAndMember(long tableId, Member member) {
        return findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    void delete(CustomizeTable table);
}
