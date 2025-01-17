package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TimeBoxCreateRequest(
        @NotBlank
        Stance stance,

        @NotBlank
        BoxType type,

        @Positive
        int time,

        Integer speakerNumber
) {

    public ParliamentaryTimeBox toTimeBox(ParliamentaryTable parliamentaryTable, int sequence) {
        return new ParliamentaryTimeBox(parliamentaryTable, sequence, stance, type, time, speakerNumber);
    }
}
