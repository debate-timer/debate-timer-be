package com.debatetimer.dto.time_based.request;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record TimeBasedTableCreateRequest(TimeBasedTableInfoCreateRequest info,
                                          List<TimeBasedTimeBoxCreateRequest> table) {

    public TimeBasedTable toTable(Member member) {
        return info.toTable(member, sumOfTime());
    }

    private int sumOfTime() {
        return table.stream()
                .mapToInt(TimeBasedTimeBoxCreateRequest::time)
                .sum();
    }

    public TimeBoxes toTimeBoxes(TimeBasedTable timeBasedTable) {
        return IntStream.range(0, table.size())
                .mapToObj(i -> table.get(i).toTimeBox(timeBasedTable, i + 1))
                .collect(Collectors.collectingAndThen(Collectors.toList(), TimeBoxes::new));
    }
}
