package com.debatetimer.service.member;

import com.debatetimer.controller.member.dto.MemberCreateRequest;
import com.debatetimer.controller.member.dto.MemberCreateResponse;
import com.debatetimer.domain.member.Member;
import com.debatetimer.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberCreateResponse createMember(MemberCreateRequest request) {
        Member member = memberRepository.save(request.toMember());
        return new MemberCreateResponse(member);
    }
}
