package com.debatetimer.fixture;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import org.springframework.stereotype.Component;

@Component
public class ParliamentaryTableGenerator {

    private final ParliamentaryTableRepository parliamentaryTableRepository;

    public ParliamentaryTableGenerator(ParliamentaryTableRepository parliamentaryTableRepository) {
        this.parliamentaryTableRepository = parliamentaryTableRepository;
    }

    public ParliamentaryTable generate(Member member) {
        ParliamentaryTable table = new ParliamentaryTable(
                member,
                "토론 테이블",
                "주제",
                1800,
                false,
                false
        );
        return parliamentaryTableRepository.save(table);
    }
}
