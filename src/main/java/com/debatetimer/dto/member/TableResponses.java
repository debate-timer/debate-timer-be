package com.debatetimer.dto.member;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.timebased.TimeBasedTable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record TableResponses(List<TableResponse> tables) {

    private static final Comparator<DebateTable> DEBATE_TABLE_COMPARATOR = Comparator
            .comparing(DebateTable::getUsedAt)
            .reversed();

    public TableResponses(List<ParliamentaryTable> parliamentaryTables,
                          List<TimeBasedTable> timeBasedTables) {
        this(toTableResponses(parliamentaryTables, timeBasedTables));
    }

    private static List<TableResponse> toTableResponses(List<ParliamentaryTable> parliamentaryTables,
                                                        List<TimeBasedTable> timeBasedTables) {
        return Stream.concat(parliamentaryTables.stream(), timeBasedTables.stream())
                .sorted(DEBATE_TABLE_COMPARATOR)
                .map(TableResponse::new)
                .toList();
    }
}
