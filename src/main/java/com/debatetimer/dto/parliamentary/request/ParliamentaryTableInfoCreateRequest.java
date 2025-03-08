package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParliamentaryTableInfoCreateRequest(
        @NotBlank
        String name,

        @NotNull
        String agenda,

        boolean warningBell,
        boolean finishBell
) {

    public ParliamentaryTable toTable(Member member, boolean warningBell, boolean finishBell) {
        return new ParliamentaryTable(member, name, agenda, warningBell, finishBell);
    }
}
