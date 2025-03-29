package com.debatetimer.dto.parliamentary.response;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;

public record ParliamentaryTimeBoxResponse(Stance stance, ParliamentaryBoxType type, int time, Integer speakerNumber) {

    public ParliamentaryTimeBoxResponse(ParliamentaryTimeBox parliamentaryTimeBox) {
        this(parliamentaryTimeBox.getStance(),
                parliamentaryTimeBox.getType(),
                parliamentaryTimeBox.getTime(),
                getSpeakerNumber(parliamentaryTimeBox)
        );
    }

    private static Integer getSpeakerNumber(ParliamentaryTimeBox parliamentaryTimeBox) {
        if (parliamentaryTimeBox.getSpeaker() == null || parliamentaryTimeBox.getSpeaker().equals("null")) {
            return null;
        }
        return Integer.parseInt(parliamentaryTimeBox.getSpeaker());
    }
}
