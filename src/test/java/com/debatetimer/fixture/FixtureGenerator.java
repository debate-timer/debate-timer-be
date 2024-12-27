package com.debatetimer.fixture;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;

public class FixtureGenerator {

    private final MemberRepository memberRepository;
    private final ParliamentaryTableRepository parliamentaryTableRepository;
    private final ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository;

    public FixtureGenerator(
            MemberRepository memberRepository,
            ParliamentaryTableRepository parliamentaryTableRepository,
            ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository
    ) {
        this.memberRepository = memberRepository;
        this.parliamentaryTableRepository = parliamentaryTableRepository;
        this.parliamentaryTimeBoxRepository = parliamentaryTimeBoxRepository;
    }

    public Member generateMember(String nickname) {
        return memberRepository.save(new Member(nickname));
    }

    public ParliamentaryTable generateParliamentaryTable(Member member) {
        ParliamentaryTable table = new ParliamentaryTable(member, "토론 테이블", "주제", 1800);
        return parliamentaryTableRepository.save(table);
    }
}
