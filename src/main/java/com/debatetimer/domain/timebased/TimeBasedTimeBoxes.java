package com.debatetimer.domain.timebased;

import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class TimeBasedTimeBoxes {

    private static final Comparator<TimeBasedTimeBox> TIME_BOX_COMPARATOR = Comparator
            .comparing(TimeBasedTimeBox::getSequence);

    private final List<TimeBasedTimeBox> timeBoxes;

    public TimeBasedTimeBoxes(List<TimeBasedTimeBox> timeBoxes) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
    }
}
