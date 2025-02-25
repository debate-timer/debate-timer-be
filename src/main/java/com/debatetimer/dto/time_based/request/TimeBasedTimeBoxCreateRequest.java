package com.debatetimer.dto.time_based.request;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TimeBasedTimeBoxCreateRequest(
        @NotBlank
        Stance stance,

        @NotBlank
        TimeBasedBoxType type,

        @Positive
        int time,

        @Positive
        int timePerTeam,

        @Positive
        int timePerSpeaking,

        Integer speakerNumber
) {

    public TimeBasedTimeBox toTimeBox(TimeBasedTable timeBasedTable, int sequence) {
        if (type.isTimeBased()) {
            return new TimeBasedTimeBox(timeBasedTable, sequence, stance, type, timePerTeam, timePerSpeaking,
                    speakerNumber);
        }
        return new TimeBasedTimeBox(timeBasedTable, sequence, stance, type, time, speakerNumber);
    }
}
