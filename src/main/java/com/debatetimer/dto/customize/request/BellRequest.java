package com.debatetimer.dto.customize.request;

import com.debatetimer.domain.customize.Bell;

public record BellRequest(
        int time,
        int count
) {

    public Bell toDomain() {
        return new Bell(time, count);
    }
}
