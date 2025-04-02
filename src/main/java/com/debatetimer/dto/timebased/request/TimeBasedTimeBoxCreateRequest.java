package com.debatetimer.dto.timebased.request;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import jakarta.validation.constraints.NotNull;

public record TimeBasedTimeBoxCreateRequest(
        @NotNull
        Stance stance,

        @NotNull
        TimeBasedBoxType type,

        int time,
        Integer timePerTeam,
        Integer timePerSpeaking,
        Integer speakerNumber
) {

    public TimeBasedTimeBox toTimeBox(TimeBasedTable timeBasedTable, int sequence) {
        if (type.isTimeBased()) {
            return new TimeBasedTimeBox(timeBasedTable, sequence, stance, type, time, timePerTeam, timePerSpeaking,
                    speakerNumber);
        }
        return new TimeBasedTimeBox(timeBasedTable, sequence, stance, type, time, speakerNumber);
    }
}
