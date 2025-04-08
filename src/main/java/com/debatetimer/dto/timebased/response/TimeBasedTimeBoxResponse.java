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
                getSpeakerNumber(timeBasedTimeBox)
        );
    }

    private static Integer getSpeakerNumber(TimeBasedTimeBox timeBasedTimeBox) {
        if (timeBasedTimeBox.getSpeaker() == null || timeBasedTimeBox.getSpeaker().equals("null")) {
            return null;
        }
        return Integer.parseInt(timeBasedTimeBox.getSpeaker());
    }
}
