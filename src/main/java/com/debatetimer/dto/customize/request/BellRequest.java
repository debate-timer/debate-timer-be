package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.BellType;
import jakarta.validation.constraints.NotNull;

public record BellRequest(
        @NotNull
        BellType type,
        int time,
        int count
) {

    public Bell toDomain() {
        return new Bell(type, time, count);
    }
}
