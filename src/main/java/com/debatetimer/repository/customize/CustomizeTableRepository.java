package com.debatetimer.repository.customize;

import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface CustomizeTableRepository extends Repository<CustomizeTableEntity, Long> {

    CustomizeTableEntity save(CustomizeTableEntity customizeTableEntity);

    Optional<CustomizeTableEntity> findById(long id);

    List<CustomizeTableEntity> findAllByMember(Member member);

    Optional<CustomizeTableEntity> findByIdAndMember(long tableId, Member member);

    default CustomizeTableEntity getByIdAndMember(long tableId, Member member) {
        return findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
    }

    void delete(CustomizeTableEntity table);
}
