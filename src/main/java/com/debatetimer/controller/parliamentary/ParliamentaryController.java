package com.debatetimer.controller.parliamentary;

import com.debatetimer.domain.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ParliamentaryController {

    private final ParliamentaryService parliamentaryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/table/parliamentary")
    public ParliamentaryTableResponse save(
            @Valid @RequestBody ParliamentaryTableCreateRequest tableCreateRequest,
            @AuthMember Member member
    ) {
        return parliamentaryService.save(tableCreateRequest, member);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/table/parliamentary/{tableId}")
    public ParliamentaryTableResponse getTable(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return parliamentaryService.findTable(tableId, member);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/api/table/parliamentary/{tableId}")
    public ParliamentaryTableResponse updateTable(
            @Valid @RequestBody ParliamentaryTableCreateRequest tableCreateRequest,
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        return parliamentaryService.updateTable(tableCreateRequest, tableId, member);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/table/parliamentary/{tableId}")
    public void deleteTable(
            @PathVariable Long tableId,
            @AuthMember Member member
    ) {
        parliamentaryService.deleteTable(tableId, member);
    }
}
