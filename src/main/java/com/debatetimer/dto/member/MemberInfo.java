package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberInfo(String nickname, String email) {

    public MemberInfo(Member member) {
        this(member.getNickname(), member.getEmail());
    }

    public Member toMember() {
        return new Member(nickname, email);
    }
}
