package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberInfo(String nickname) {

    public Member toMember() {
        return new Member(nickname);
    }
}
