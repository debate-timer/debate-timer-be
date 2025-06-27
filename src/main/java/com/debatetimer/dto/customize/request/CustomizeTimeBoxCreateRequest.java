package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

public record CustomizeTimeBoxCreateRequest(
        @NotNull
        Stance stance,

        @NotBlank
        String speechType,

        @NotNull
        CustomizeBoxType boxType,

        @Nullable
        Integer time,

        @Nullable
        Integer timePerTeam,

        @Nullable
        Integer timePerSpeaking,

        @Nullable
        String speaker
) {

    public CustomizeTimeBox toTimeBox(CustomizeTable customizeTable, int sequence) {
        if (boxType.isTimeBased()) {
            return new CustomizeTimeBox(new CustomizeTableEntity(customizeTable), sequence, stance, speechType,
                    boxType, timePerTeam, timePerSpeaking, speaker);
        }
        return new CustomizeTimeBox(new CustomizeTableEntity(customizeTable), sequence, stance, speechType, boxType,
                time, speaker);
    }
}
