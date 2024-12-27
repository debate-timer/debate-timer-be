package com.debatetimer.fixture;

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
}
