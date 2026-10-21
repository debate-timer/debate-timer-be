package com.debatetimer.dto.admin;

import com.debatetimer.domain.stats.StatUnit;
import java.time.LocalDate;
import java.util.List;

public record TableUsageStatsResponse(
        StatUnit unit,
        LocalDate from,
        LocalDate to,
        List<TableUsageStatResponse> periods
) {
}
