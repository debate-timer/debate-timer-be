package com.debatetimer.controller.parliamentary;

import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ParliamentaryController {

    private final ParliamentaryService parliamentaryService;

    @PostMapping("/api/table/parliamentary")
    public ParliamentaryTableResponse save(
            @RequestBody ParliamentaryTableCreateRequest tableCreateRequest,
            @RequestParam Long memberId
    ) {
        return parliamentaryService.save(tableCreateRequest, memberId);
    }

    @GetMapping("/api/table/parliamentary/{tableId}")
    public ParliamentaryTableResponse getTable(
            @PathVariable Long tableId,
            @RequestParam Long memberId
    ) {
        return parliamentaryService.findTable(tableId, memberId);
    }
}
