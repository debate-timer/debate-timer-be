package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TableInfoCreateRequest(
        @NotBlank
        String name,

        @NotNull
        String agenda
) {

    public ParliamentaryTable toTable(Member member, int duration) {
        return new ParliamentaryTable(member, name, agenda, duration);
    }
}
