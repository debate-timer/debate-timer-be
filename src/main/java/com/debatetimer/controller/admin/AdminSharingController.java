package com.debatetimer.controller.admin;

import com.debatetimer.dto.admin.SharingStatsResponse;
import com.debatetimer.service.admin.SharingStatsService;
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
public class AdminSharingController {

    public static final String ADMIN_PASSWORD_HEADER = "X-Admin-Password";

    private final AdminAuthorizer adminAuthorizer;
    private final SharingStatsService sharingStatsService;

    @GetMapping("/api/admin/sharing/stats")
    @ResponseStatus(HttpStatus.OK)
    public SharingStatsResponse getSharingStats(
            @RequestHeader(name = ADMIN_PASSWORD_HEADER, required = false) String password,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        adminAuthorizer.authorize(password);
        return sharingStatsService.getDailyStats(from, to);
    }
}
