package com.debatetimer.domain.customize;

import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class CustomizeTimeBoxes {

    private static final Comparator<CustomizeTimeBox> TIME_BOX_COMPARATOR = Comparator
            .comparing(CustomizeTimeBox::getSequence);

    private final List<CustomizeTimeBox> timeBoxes;

    public CustomizeTimeBoxes(List<CustomizeTimeBox> timeBoxes) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
    }
}
