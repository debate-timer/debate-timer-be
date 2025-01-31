package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Member;

public record MemberCreateResponse(long id, String email) {

    public MemberCreateResponse(Member member) {
        this(member.getId(), member.getEmail());
    }
}
