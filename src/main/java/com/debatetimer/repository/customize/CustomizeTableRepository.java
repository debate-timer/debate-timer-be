package com.debatetimer.repository.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface CustomizeTableRepository extends Repository<CustomizeTable, Long> {

    CustomizeTable save(CustomizeTable customizeTable);

    Optional<CustomizeTable> findById(long id);

    List<CustomizeTable> findAllByMember(Member member);

    Optional<CustomizeTable> findByIdAndMember(long tableId, Member member);

    void delete(CustomizeTable table);
}
