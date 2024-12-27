package com.debatetimer.domain.parliamentary;

import java.util.List;
import lombok.Getter;

@Getter
public class ParliamentaryTimeBoxes {

    private final List<ParliamentaryTimeBox> timeBoxes;

    public ParliamentaryTimeBoxes(List<ParliamentaryTimeBox> timeBoxes) {
        timeBoxes.sort(ParliamentaryTimeBox::compareTo);
        this.timeBoxes = timeBoxes;
    }
}
