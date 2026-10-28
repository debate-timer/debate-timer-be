package com.debatetimer.controller.admin;

import com.debatetimer.dto.admin.SharingStatsResponse;
import com.debatetimer.service.admin.SharingStatsService;
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
public class AdminSharingController {

    private final SharingStatsService sharingStatsService;

    @GetMapping("/api/admin/sharing/stats")
    @ResponseStatus(HttpStatus.OK)
    public SharingStatsResponse getSharingStats(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return sharingStatsService.getDailyStats(from, to);
    }
}
