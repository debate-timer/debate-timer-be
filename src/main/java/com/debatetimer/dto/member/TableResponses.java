package com.debatetimer.dto.member;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.timebased.TimeBasedTable;
import java.util.List;
import java.util.stream.Stream;

public record TableResponses(List<TableResponse> tables) {

    public static TableResponses from(List<ParliamentaryTable> parliamentaryTables,
                                      List<TimeBasedTable> timeBasedTables) {
        return new TableResponses(toTableResponses(parliamentaryTables, timeBasedTables));
    }

    private static List<TableResponse> toTableResponses(List<ParliamentaryTable> parliamentaryTables,
                                                        List<TimeBasedTable> timeBasedTables) {
        Stream<TableResponse> parliamentaryTableResponseStream = parliamentaryTables.stream()
                .map(TableResponse::new);
        Stream<TableResponse> timeBasedTableResponseStream = timeBasedTables.stream()
                .map(TableResponse::new);
        return Stream.concat(parliamentaryTableResponseStream, timeBasedTableResponseStream)
                .toList();
    }
}
