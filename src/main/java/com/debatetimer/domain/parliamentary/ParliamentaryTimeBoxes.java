package com.debatetimer.domain.parliamentary;

import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ParliamentaryTimeBoxes {

    private static final Comparator<ParliamentaryTimeBox> TIME_BOX_COMPARATOR = Comparator
            .comparing(ParliamentaryTimeBox::getSequence);

    private final List<ParliamentaryTimeBox> timeBoxes;

    public ParliamentaryTimeBoxes(List<ParliamentaryTimeBox> timeBoxes) {
        this.timeBoxes = timeBoxes.stream()
                .sorted(TIME_BOX_COMPARATOR)
                .toList();
    }
}
