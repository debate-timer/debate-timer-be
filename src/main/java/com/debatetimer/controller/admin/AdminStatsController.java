package com.debatetimer.controller.admin;

import com.debatetimer.domain.stats.StatUnit;
import com.debatetimer.dto.admin.TableUsageStatsResponse;
import com.debatetimer.service.admin.TableUsageStatsService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminAuthorizer adminAuthorizer;
    private final TableUsageStatsService tableUsageStatsService;

    @GetMapping("/api/admin/tables/stats")
    @ResponseStatus(HttpStatus.OK)
    public TableUsageStatsResponse getTableUsageStats(
            @RequestHeader(name = AdminSharingController.ADMIN_PASSWORD_HEADER, required = false) String password,
            @RequestParam("unit") StatUnit unit,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        adminAuthorizer.authorize(password);
        return tableUsageStatsService.getStats(unit, from, to);
    }
}
