package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberCreateResponse(
        @Schema(description = "멤버 아이디", example = "1")
        long id,

        @Schema(description = "멤버 닉네임", example = "콜리")
        String nickname
) {

    public MemberCreateResponse(Member member) {
        this(member.getId(), member.getNickname());
    }
}
