package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Member;
import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(@NotBlank String nickname) {

    public Member toMember() {
        return new Member(nickname);
    }
}
