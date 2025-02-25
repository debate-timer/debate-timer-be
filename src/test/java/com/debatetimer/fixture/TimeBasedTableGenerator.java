package com.debatetimer.fixture;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.repository.time_based.TimeBasedTableRepository;
import org.springframework.stereotype.Component;

@Component
public class TimeBasedTableGenerator {

    private final TimeBasedTableRepository timeBasedTableRepository;

    public TimeBasedTableGenerator(TimeBasedTableRepository timeBasedTableRepository) {
        this.timeBasedTableRepository = timeBasedTableRepository;
    }

    public TimeBasedTable generate(Member member) {
        TimeBasedTable table = new TimeBasedTable(
                member,
                "토론 테이블",
                "주제",
                1800,
                false,
                false
        );
        return timeBasedTableRepository.save(table);
    }
}
