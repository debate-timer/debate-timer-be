package com.debatetimer.controller.customize;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.service.customize.CustomizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomizeController {

    private final CustomizeService customizeService;

    @PostMapping("/api/table/customize")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomizeTableResponse save(
            @Valid @RequestBody CustomizeTableCreateRequest tableCreateRequest,
            @AuthMember Member member
    ) {
        return customizeService.save(tableCreateRequest, member);
    }

    @GetMapping("/api/table/customize/{tableId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomizeTableResponse getTable(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return customizeService.findTable(tableId, member);
    }

    @PutMapping("/api/table/customize/{tableId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomizeTableResponse updateTable(
            @Valid @RequestBody CustomizeTableCreateRequest tableCreateRequest,
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return customizeService.updateTable(tableCreateRequest, tableId, member);
    }

    @PatchMapping("/api/table/customize/{tableId}/debate")
    @ResponseStatus(HttpStatus.OK)
    public CustomizeTableResponse debate(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return customizeService.updateUsedAt(tableId, member);
    }

    @DeleteMapping("/api/table/customize/{tableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTable(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        customizeService.deleteTable(tableId, member);
    }
}
