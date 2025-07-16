package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
        List<BellRequest> bell,

        @Nullable
        Integer timePerTeam,

        @Nullable
        Integer timePerSpeaking,

        @Nullable
        String speaker
) {

    public CustomizeTimeBoxEntity toTimeBox(CustomizeTable customizeTable, int sequence) {
        if (boxType.isTimeBased()) {
            return new CustomizeTimeBoxEntity(new CustomizeTableEntity(customizeTable), sequence, stance, speechType,
                    boxType, timePerTeam, timePerSpeaking, speaker);
        }
        return new CustomizeTimeBoxEntity(new CustomizeTableEntity(customizeTable), sequence, stance, speechType, boxType,
                time, speaker);
    }
}
