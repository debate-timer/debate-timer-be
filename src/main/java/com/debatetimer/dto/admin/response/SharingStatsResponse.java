package com.debatetimer.dto.admin.response;

import java.time.LocalDate;
import java.util.List;

public record SharingStatsResponse(
        LocalDate from,
        LocalDate to,
        List<SharingDailyStatResponse> days
) {
}
