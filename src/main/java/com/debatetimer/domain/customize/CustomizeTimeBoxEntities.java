package com.debatetimer.domain.customize;

import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class CustomizeTimeBoxEntities {

    private static final Comparator<CustomizeTimeBoxEntity> TIME_BOX_COMPARATOR = Comparator
            .comparing(CustomizeTimeBoxEntity::getSequence);

    private final List<CustomizeTimeBoxEntity> timeBoxes;

    public CustomizeTimeBoxEntities(List<CustomizeTimeBoxEntity> timeBoxes) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
    }
}
