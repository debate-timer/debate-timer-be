package com.debatetimer.fixture;

import com.debatetimer.domain.member.Member;
import com.debatetimer.repository.member.MemberRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberGenerator {

    private final MemberRepository memberRepository;

    public MemberGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member generate(String nickName) {
        Member member = new Member(nickName);
        return memberRepository.save(member);
    }
}
