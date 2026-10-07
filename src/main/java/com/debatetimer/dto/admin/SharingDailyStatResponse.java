package com.debatetimer.dto.admin;

import java.time.LocalDate;

public record SharingDailyStatResponse(
        LocalDate date,
        int finishedCount,
        double averageMinutes
) {
}
