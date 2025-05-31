package com.debatetimer.dto.member;

import com.debatetimer.domain.customize.CustomizeTableEntity;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record TableResponses(List<TableResponse> tables) {

    private static final Comparator<CustomizeTableEntity> DEBATE_TABLE_COMPARATOR = Comparator
            .comparing(CustomizeTableEntity::getUsedAt)
            .reversed();

    public static TableResponses from(List<CustomizeTableEntity> customizeTableEntities) {
        return customizeTableEntities.stream()
                .sorted(DEBATE_TABLE_COMPARATOR)
                .map(TableResponse::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(), TableResponses::new));
    }
}
