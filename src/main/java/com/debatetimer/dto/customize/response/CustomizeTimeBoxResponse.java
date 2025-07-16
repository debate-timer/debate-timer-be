package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.List;

public record CustomizeTimeBoxResponse(
        Stance stance,
        String speechType,
        CustomizeBoxType boxType,
        Integer time,
        List<BellResponse> bell,
        Integer timePerTeam,
        Integer timePerSpeaking,
        String speaker
) {

    public CustomizeTimeBoxResponse(CustomizeTimeBoxEntity customizeTimeBox) {
        this(
                customizeTimeBox.getStance(),
                customizeTimeBox.getSpeechType(),
                customizeTimeBox.getBoxType(),
                convertTime(customizeTimeBox),
                null,
                customizeTimeBox.getTimePerTeam(),
                customizeTimeBox.getTimePerSpeaking(),
                customizeTimeBox.getSpeaker()
        );
    }

    public CustomizeTimeBoxResponse(CustomizeTimeBoxEntity customizeTimeBox, List<BellResponse> bell) {
        this(
                customizeTimeBox.getStance(),
                customizeTimeBox.getSpeechType(),
                customizeTimeBox.getBoxType(),
                convertTime(customizeTimeBox),
                bell,
                customizeTimeBox.getTimePerTeam(),
                customizeTimeBox.getTimePerSpeaking(),
                customizeTimeBox.getSpeaker()
        );
    }

    private static Integer convertTime(CustomizeTimeBoxEntity customizeTimeBox) {
        if (customizeTimeBox.getBoxType() == CustomizeBoxType.TIME_BASED) {
            return null;
        }
        return customizeTimeBox.getTime();
    }
}
