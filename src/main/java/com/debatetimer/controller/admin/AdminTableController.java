package com.debatetimer.controller.admin;

import com.debatetimer.domain.customize.CustomizeTableSortType;
import com.debatetimer.dto.admin.AdminTableDetailResponse;
import com.debatetimer.dto.admin.AdminTablePageResponse;
import com.debatetimer.service.admin.AdminTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminTableController {

    private final AdminTableService adminTableService;

    @GetMapping("/api/admin/tables")
    @ResponseStatus(HttpStatus.OK)
    public AdminTablePageResponse getTables(
            @RequestParam(name = "sort", defaultValue = "CREATED_AT") CustomizeTableSortType sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return adminTableService.getTables(sort, page, size);
    }

    @GetMapping("/api/admin/tables/{tableId}")
    @ResponseStatus(HttpStatus.OK)
    public AdminTableDetailResponse getTable(@PathVariable long tableId) {
        return adminTableService.getTable(tableId);
    }
}
