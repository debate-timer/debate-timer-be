package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Member;

public record MemberInfo(String name) {

    public Member toMember() {
        return new Member(name);
    }
}
