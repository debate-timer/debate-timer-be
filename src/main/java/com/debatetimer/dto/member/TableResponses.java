package com.debatetimer.dto.member;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.customize.CustomizeTable;
import java.util.Comparator;
import java.util.List;

public record TableResponses(List<TableResponse> tables) {

    private static final Comparator<DebateTable> DEBATE_TABLE_COMPARATOR = Comparator
            .comparing(DebateTable::getUsedAt)
            .reversed();

    public TableResponses(
            List<CustomizeTable> customizeTables
    ) {
        this(toTableResponses(customizeTables));
    }

    private static List<TableResponse> toTableResponses(List<CustomizeTable> customizeTables) {
        return customizeTables.stream()
                .sorted(DEBATE_TABLE_COMPARATOR)
                .map(TableResponse::new)
                .toList();
    }
}
