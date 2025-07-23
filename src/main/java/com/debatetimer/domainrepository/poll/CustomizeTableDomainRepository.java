package com.debatetimer.domainrepository.poll;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomizeTableDomainRepository {

    private final CustomizeTableRepository customizeTableRepository;

    public CustomizeTable getByIdAndMember(long tableId, Member member) {
        return customizeTableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND))
                .toDomain();
    }
}
