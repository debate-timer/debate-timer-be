package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ParliamentaryTimeBoxCreateRequest(
        @NotNull
        Stance stance,

        @NotNull
        ParliamentaryBoxType type,

        @Positive
        int time,

        Integer speakerNumber
) {

    public ParliamentaryTimeBox toTimeBox(ParliamentaryTable parliamentaryTable, int sequence) {
        return new ParliamentaryTimeBox(parliamentaryTable, sequence, stance, type, time, speakerNumber);
    }
}
