package com.debatetimer.domain;

import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class TimeBoxes<T extends DebateTimeBox> {

    private static final Comparator<DebateTimeBox> TIME_BOX_COMPARATOR = Comparator
            .comparing(DebateTimeBox::getSequence);

    private final List<T> timeBoxes;

    public TimeBoxes(List<T> timeBoxes) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
    }
}
