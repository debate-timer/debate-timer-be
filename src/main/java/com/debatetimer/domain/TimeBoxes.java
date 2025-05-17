package com.debatetimer.domain;

import com.debatetimer.domain.customize.CustomizeTimeBox;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class TimeBoxes {

    private static final Comparator<CustomizeTimeBox> TIME_BOX_COMPARATOR = Comparator
            .comparing(CustomizeTimeBox::getSequence);

    private final List<CustomizeTimeBox> timeBoxes;

    public TimeBoxes(List<CustomizeTimeBox> timeBoxes) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
    }
}
