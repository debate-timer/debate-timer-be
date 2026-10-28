package com.debatetimer.controller.admin;

import com.debatetimer.domain.stats.StatUnit;
import com.debatetimer.dto.admin.MemberSignupStatsResponse;
import com.debatetimer.dto.admin.TableUsageStatsResponse;
import com.debatetimer.service.admin.MemberSignupStatsService;
import com.debatetimer.service.admin.TableUsageStatsService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminStatsController {

    private final TableUsageStatsService tableUsageStatsService;
    private final MemberSignupStatsService memberSignupStatsService;

    @GetMapping("/api/admin/tables/stats")
    @ResponseStatus(HttpStatus.OK)
    public TableUsageStatsResponse getTableUsageStats(
            @RequestParam("unit") StatUnit unit,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return tableUsageStatsService.getStats(unit, from, to);
    }

    @GetMapping("/api/admin/members/stats")
    @ResponseStatus(HttpStatus.OK)
    public MemberSignupStatsResponse getMemberSignupStats(
            @RequestParam("unit") StatUnit unit,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return memberSignupStatsService.getStats(unit, from, to);
    }
}
