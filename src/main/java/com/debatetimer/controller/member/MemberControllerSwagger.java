package com.debatetimer.controller.member;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.TableResponses;

public interface MemberControllerSwagger {

    TableResponses getTables(Member member);

    MemberCreateResponse createMember(MemberCreateRequest request);
}
