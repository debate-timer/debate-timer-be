package com.debatetimer.dto.timebased.request;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TimeBasedTableInfoCreateRequest(
        @NotBlank
        String name,

        @NotNull
        String agenda,

        boolean warningBell,
        boolean finishBell
) {

    public TimeBasedTable toTable(Member member) {
        return new TimeBasedTable(member, name, agenda, warningBell, finishBell);
    }
}
