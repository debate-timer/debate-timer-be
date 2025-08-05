package com.debatetimer.entity.customize;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

public class CustomizeTimeBoxEntities {

    private static final Comparator<CustomizeTimeBoxEntity> TIME_BOX_COMPARATOR = Comparator
            .comparing(CustomizeTimeBoxEntity::getSequence);

    @Getter
    private final List<CustomizeTimeBoxEntity> timeBoxes;

    private final List<BellEntity> bells;

    public CustomizeTimeBoxEntities(List<CustomizeTimeBoxEntity> timeBoxes, List<BellEntity> bells) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
        this.bells = bells;
    }

    public List<CustomizeTimeBox> toDomain() {
        return timeBoxes.stream()
                .map(timebox -> timebox.toDomain(getBells(timebox)))
                .toList();
    }

    private List<Bell> getBells(CustomizeTimeBoxEntity timeBox) {
        return bells.stream()
                .filter(bell -> bell.isContained(timeBox))
                .map(BellEntity::toDomain)
                .toList();
    }
}
