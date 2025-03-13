package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTimeBox;

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
                customizeTimeBox.getTime(),
                customizeTimeBox.getTimePerTeam(),
                customizeTimeBox.getTimePerSpeaking(),
                String.valueOf(customizeTimeBox.getSpeaker())
        );
    }
}
