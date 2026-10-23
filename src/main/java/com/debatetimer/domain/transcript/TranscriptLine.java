package com.debatetimer.domain.transcript;

import com.debatetimer.domain.customize.Stance;
import org.springframework.lang.Nullable;

public record TranscriptLine(
        int index,
        int timeBoxIndex,
        String speechType,
        Stance stance,
        @Nullable String speaker,
        long offsetMs,
        String text
) {
}
