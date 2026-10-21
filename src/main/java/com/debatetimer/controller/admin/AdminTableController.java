package com.debatetimer.controller.admin;

import com.debatetimer.dto.admin.AdminTableDetailResponse;
import com.debatetimer.dto.admin.AdminTablePageResponse;
import com.debatetimer.service.admin.AdminTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminTableController {

    private final AdminAuthorizer adminAuthorizer;
    private final AdminTableService adminTableService;

    @GetMapping("/api/admin/tables")
    @ResponseStatus(HttpStatus.OK)
    public AdminTablePageResponse getTables(
            @RequestHeader(name = AdminSharingController.ADMIN_PASSWORD_HEADER, required = false) String password,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        adminAuthorizer.authorize(password);
        return adminTableService.getTables(page, size);
    }

    @GetMapping("/api/admin/tables/{tableId}")
    @ResponseStatus(HttpStatus.OK)
    public AdminTableDetailResponse getTable(
            @RequestHeader(name = AdminSharingController.ADMIN_PASSWORD_HEADER, required = false) String password,
            @PathVariable long tableId
    ) {
        adminAuthorizer.authorize(password);
        return adminTableService.getTable(tableId);
    }
}
