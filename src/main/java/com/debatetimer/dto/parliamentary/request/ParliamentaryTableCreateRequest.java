package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;

public record ParliamentaryTableCreateRequest(
        TableInfoCreateRequest info,
        TimeBoxCreateRequests table
) {

    public ParliamentaryTable toTable(Member member) {
        return info.toTable(member, table.sumOfTime());
    }
}
