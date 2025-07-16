package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.Bell;

public record BellResponse(
        int time,
        int count
) {

    public BellResponse(Bell bell) {
        this(bell.getTime(), bell.getCount());
    }
}
