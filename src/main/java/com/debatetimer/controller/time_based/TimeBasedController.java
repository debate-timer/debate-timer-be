package com.debatetimer.controller.time_based;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.time_based.request.TimeBasedTableCreateRequest;
import com.debatetimer.dto.time_based.response.TimeBasedTableResponse;
import com.debatetimer.service.time_based.TimeBasedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimeBasedController {

    private final TimeBasedService timeBasedService;

    @PostMapping("/api/table/time-based")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeBasedTableResponse save(
            @Valid @RequestBody TimeBasedTableCreateRequest tableCreateRequest,
            @AuthMember Member member
    ) {
        return timeBasedService.save(tableCreateRequest, member);
    }

    @GetMapping("/api/table/time-based/{tableId}")
    @ResponseStatus(HttpStatus.OK)
    public TimeBasedTableResponse getTable(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return timeBasedService.findTable(tableId, member);
    }

    @PatchMapping("/api/table/time-based/{tableId}")
    @ResponseStatus(HttpStatus.OK)
    public TimeBasedTableResponse updateTable(
            @Valid @RequestBody TimeBasedTableCreateRequest tableCreateRequest,
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return timeBasedService.updateTable(tableCreateRequest, tableId, member);
    }

    @DeleteMapping("/api/table/time-based/{tableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTable(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        timeBasedService.deleteTable(tableId, member);
    }
}
