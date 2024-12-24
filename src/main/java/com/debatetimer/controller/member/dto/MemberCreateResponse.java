package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.member.Member;

public record MemberCreateResponse(long id, String nickname) {

    public MemberCreateResponse(Member member) {
        this(member.getId(), member.getNickname());
    }
}
