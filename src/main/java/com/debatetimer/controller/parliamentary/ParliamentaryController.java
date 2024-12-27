package com.debatetimer.controller.parliamentary;

import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ParliamentaryController {

    private final ParliamentaryService parliamentaryService;

    @GetMapping("/api/table/parliamentary/{tableId}")
    public ParliamentaryTableResponse getTable(
            @PathVariable Long tableId,
            @RequestParam Long memberId
    ) {
        return parliamentaryService.findTable(tableId, memberId);
    }


}
