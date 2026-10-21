package com.debatetimer.dto.admin;

import java.time.LocalDate;

public record TableUsageStatResponse(
        LocalDate periodStart,
        long createdCount,
        long usedCount
) {
}
