package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;

public record TimeBoxResponse(Stance stance, ParliamentaryBoxType type, int time, Integer speakerNumber) {

    public TimeBoxResponse(ParliamentaryTimeBox parliamentaryTimeBox) {
        this(parliamentaryTimeBox.getStance(),
                parliamentaryTimeBox.getType(),
                parliamentaryTimeBox.getTime(),
                parliamentaryTimeBox.getSpeaker()
        );
    }
}
