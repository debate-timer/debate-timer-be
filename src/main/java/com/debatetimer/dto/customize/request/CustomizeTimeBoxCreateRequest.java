package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import jakarta.validation.constraints.NotBlank;

public record CustomizeTimeBoxCreateRequest(
        @NotBlank
        Stance stance,

        @NotBlank
        String speechType,

        @NotBlank
        CustomizeBoxType boxType,

        int time,
        Integer timePerTeam,
        Integer timePerSpeaking,
        String speaker
) {

    public CustomizeTimeBox toTimeBox(CustomizeTable customizeTable, int sequence) {
        if (boxType.isTimeBased()) {
            return new CustomizeTimeBox(customizeTable, sequence, stance, speechType, boxType, time, timePerTeam,
                    timePerSpeaking, Integer.parseInt(speaker));
        }
        return new CustomizeTimeBox(customizeTable, sequence, stance, speechType, boxType, time,
                Integer.parseInt(speaker));
    }
}
