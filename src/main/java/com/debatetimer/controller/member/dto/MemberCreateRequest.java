package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.member.Member;

public record MemberCreateRequest(String nickname) {

    public Member toMember() {
        return new Member(nickname);
    }
}
