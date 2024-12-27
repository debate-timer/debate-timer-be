package com.debatetimer.config;

import com.debatetimer.fixture.FixtureGenerator;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FixtureGeneratorConfig {

    @Bean
    public FixtureGenerator fixtureGenerator(
            MemberRepository memberRepository,
            ParliamentaryTableRepository parliamentaryTableRepository,
            ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository) {
        return new FixtureGenerator(
                memberRepository,
                parliamentaryTableRepository,
                parliamentaryTimeBoxRepository
        );
    }


}
