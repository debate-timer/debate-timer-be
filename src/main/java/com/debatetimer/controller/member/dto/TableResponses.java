package com.debatetimer.controller.member.dto;

import com.debatetimer.domain.parliamentary_debate.ParliamentaryTable;
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
