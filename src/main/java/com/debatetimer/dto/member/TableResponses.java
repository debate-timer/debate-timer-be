package com.debatetimer.dto.member;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;

public record TableResponses(List<TableResponse> tables) {

    public static TableResponses from(List<ParliamentaryTable> parliamentaryTables) {
        return new TableResponses(toTableResponses(parliamentaryTables));
    }

    private static List<TableResponse> toTableResponses(List<ParliamentaryTable> parliamentaryTables) {
        return parliamentaryTables.stream()
                .map(TableResponse::new)
                .toList();
    }
}
