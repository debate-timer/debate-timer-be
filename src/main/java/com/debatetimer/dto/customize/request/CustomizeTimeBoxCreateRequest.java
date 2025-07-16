package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.customize.NormalTimeBox;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.customize.TimeBasedTimeBox;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
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

    public CustomizeTimeBox toDomain() {
        if (boxType.isTimeBased()) {
            return new TimeBasedTimeBox(stance, speechType, speaker, timePerTeam, timePerSpeaking);
        }
        return new NormalTimeBox(stance, speechType, speaker, time, toBells(bell));
    }

    private List<Bell> toBells(List<BellRequest> bellRequests) {
        if (bellRequests == null) {
            return Collections.emptyList();
        }
        return bellRequests.stream()
                .map(BellRequest::toDomain)
                .toList();
    }
}
