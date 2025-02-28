package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ParliamentaryTableCreateRequest(ParliamentaryTableInfoCreateRequest info,
                                              List<ParliamentaryTimeBoxCreateRequest> table) {

    public ParliamentaryTable toTable(Member member) {
        return info.toTable(member, sumOfTime(), info.warningBell(), info().finishBell());
    }

    private int sumOfTime() {
        return table.stream()
                .mapToInt(ParliamentaryTimeBoxCreateRequest::time)
                .sum();
    }

    public TimeBoxes toTimeBoxes(ParliamentaryTable parliamentaryTable) {
        return IntStream.range(0, table.size())
                .mapToObj(i -> table.get(i).toTimeBox(parliamentaryTable, i + 1))
                .collect(Collectors.collectingAndThen(Collectors.toList(), TimeBoxes::new));
    }
}
