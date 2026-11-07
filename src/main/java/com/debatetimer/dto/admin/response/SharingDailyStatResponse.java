package com.debatetimer.dto.admin.response;

import java.time.LocalDate;

public record SharingDailyStatResponse(
        LocalDate date,
        int finishedCount,
        double averageMinutes,
        int abandonedCount,
        double abandonedAverageMinutes
) {
}
