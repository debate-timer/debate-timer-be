package com.debatetimer.dto.customize.response;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.BellType;

public record BellResponse(
        BellType type,
        int time,
        int count
) {

    public BellResponse(Bell bell) {
        this(bell.getType(), bell.getTime(), bell.getCount());
    }
}
