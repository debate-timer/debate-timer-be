package com.debatetimer.dto.timebased.response;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;

public record TimeBasedTimeBoxResponse(
        Stance stance,
        TimeBasedBoxType type,
        Integer time,
        Integer timePerTeam,
        Integer timePerSpeaking,
        Integer speakerNumber
) {

    public TimeBasedTimeBoxResponse(TimeBasedTimeBox timeBasedTimeBox) {
        this(timeBasedTimeBox.getStance(),
                timeBasedTimeBox.getType(),
                timeBasedTimeBox.getTime(),
                timeBasedTimeBox.getTimePerTeam(),
                timeBasedTimeBox.getTimePerSpeaking(),
                Integer.parseInt(timeBasedTimeBox.getSpeaker())
        );
    }
}
