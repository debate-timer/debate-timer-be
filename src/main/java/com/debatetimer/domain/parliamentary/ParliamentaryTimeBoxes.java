package com.debatetimer.domain.parliamentary;

import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class ParliamentaryTimeBoxes {

    private static final Comparator<ParliamentaryTimeBox> TIME_BOX_COMPARATOR = Comparator
            .comparing(ParliamentaryTimeBox::getSequence);

    private final List<ParliamentaryTimeBox> timeBoxes;

    public ParliamentaryTimeBoxes(List<ParliamentaryTimeBox> timeBoxes) {
        timeBoxes.sort(TIME_BOX_COMPARATOR);
        this.timeBoxes = timeBoxes;
    }
}
