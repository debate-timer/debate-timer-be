package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;

public record TimeBoxResponse(String stance, String type, int time, Integer speakerNumber) {

    public TimeBoxResponse(ParliamentaryTimeBox parliamentaryTimeBox) {
        this(parliamentaryTimeBox.getStance().name(),
                parliamentaryTimeBox.getType().name(),
                parliamentaryTimeBox.getTime(),
                parliamentaryTimeBox.getSpeaker()
        );
    }
}
