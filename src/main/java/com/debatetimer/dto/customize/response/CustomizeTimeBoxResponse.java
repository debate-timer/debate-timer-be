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

    //{
    //			"stance": "PROS",
    //			"speechType": "찬성측 청중 질의",
    //			"boxType" : "NORMAL",
    //			"time": 175,
    //			"timePerTeam" : null,
    //			"timePerSpeaking" : null,
    //			"speaker": "콜리"
    //		},

    public CustomizeTimeBoxResponse(CustomizeTimeBox customizeTimeBox) {
        this(
                customizeTimeBox.getStance(),
                customizeTimeBox.getSpeechType(),
                customizeTimeBox.getBoxType(),
                customizeTimeBox.getTime(),
                customizeTimeBox.getTimePerTeam(),
                customizeTimeBox.getTimePerSpeaking(),
                customizeTimeBox.getSpeaker()
        );
    }
}
