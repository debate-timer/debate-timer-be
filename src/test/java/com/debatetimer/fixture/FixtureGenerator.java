package com.debatetimer.fixture;

import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import org.springframework.stereotype.Component;

@Component
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

    public ParliamentaryTimeBox generateParliamentaryTimeBox(ParliamentaryTable testTable, int sequence) {
        ParliamentaryTimeBox timeBox = new ParliamentaryTimeBox(testTable, sequence, Stance.PROS, BoxType.OPENING, 180,
                1);
        return parliamentaryTimeBoxRepository.save(timeBox);
    }
}
