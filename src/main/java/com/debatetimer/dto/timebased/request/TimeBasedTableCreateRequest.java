package com.debatetimer.dto.timebased.request;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record TimeBasedTableCreateRequest(TimeBasedTableInfoCreateRequest info,
                                          List<TimeBasedTimeBoxCreateRequest> table) {

    public TimeBasedTable toTable(Member member) {
        return info.toTable(member);
    }

    public TimeBoxes<TimeBasedTimeBox> toTimeBoxes(TimeBasedTable timeBasedTable) {
        return IntStream.range(0, table.size())
                .mapToObj(i -> table.get(i).toTimeBox(timeBasedTable, i + 1))
                .collect(Collectors.collectingAndThen(Collectors.toList(), TimeBoxes::new));
    }
}
