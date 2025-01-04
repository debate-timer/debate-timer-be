package com.debatetimer.controller.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;

public interface ParliamentaryControllerSwagger {

    ParliamentaryTableResponse save(ParliamentaryTableCreateRequest tableCreateRequest, Member member);

    ParliamentaryTableResponse getTable(Long tableId, Member member);

    ParliamentaryTableResponse updateTable(
            ParliamentaryTableCreateRequest tableCreateRequest,
            Long tableId,
            Member member
    );

    void deleteTable(Long tableId, Member member);
}
