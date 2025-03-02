package com.debatetimer.dto.member;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.timebased.TimeBasedTable;
import java.util.List;
import java.util.stream.Stream;

public record TableResponses(List<TableResponse> tables) {

    public TableResponses(List<ParliamentaryTable> parliamentaryTables,
                          List<TimeBasedTable> timeBasedTables) {
        this(toTableResponses(parliamentaryTables, timeBasedTables));
    }

    private static List<TableResponse> toTableResponses(List<ParliamentaryTable> parliamentaryTables,
                                                        List<TimeBasedTable> timeBasedTables) {
        Stream<TableResponse> parliamentaryTableResponseStream = parliamentaryTables.stream()
                .map(parliamentaryTable -> new TableResponse(parliamentaryTable, parliamentaryTable.getId()));
        Stream<TableResponse> timeBasedTableResponseStream = timeBasedTables.stream()
                .map(timeBasedTable -> new TableResponse(timeBasedTable, timeBasedTable.getId()));
        return Stream.concat(parliamentaryTableResponseStream, timeBasedTableResponseStream)
                .toList();
    }
}
