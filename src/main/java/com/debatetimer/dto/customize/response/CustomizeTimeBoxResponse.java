package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.entity.customize.CustomizeTimeBox;

public record CustomizeTimeBoxResponse(
        Stance stance,
        String speechType,
        CustomizeBoxType boxType,
        Integer time,
        Integer timePerTeam,
        Integer timePerSpeaking,
        String speaker
) {

    public CustomizeTimeBoxResponse(CustomizeTimeBox customizeTimeBox) {
        this(
                customizeTimeBox.getStance(),
                customizeTimeBox.getSpeechType(),
                customizeTimeBox.getBoxType(),
                convertTime(customizeTimeBox),
                customizeTimeBox.getTimePerTeam(),
                customizeTimeBox.getTimePerSpeaking(),
                customizeTimeBox.getSpeaker()
        );
    }

    private static Integer convertTime(CustomizeTimeBox customizeTimeBox) {
        if (customizeTimeBox.getBoxType() == CustomizeBoxType.TIME_BASED) {
            return null;
        }
        return customizeTimeBox.getTime();
    }
}
