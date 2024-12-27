package com.debatetimer.controller.parliamentary;

import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.service.parliamentary.ParliamentaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ParliamentaryController {

    private final ParliamentaryService parliamentaryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/table/parliamentary")
    public ParliamentaryTableResponse save(
            @RequestBody ParliamentaryTableCreateRequest tableCreateRequest,
            @RequestParam Long memberId
    ) {
        return parliamentaryService.save(tableCreateRequest, memberId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/table/parliamentary/{tableId}")
    public ParliamentaryTableResponse getTable(
            @PathVariable Long tableId,
            @RequestParam Long memberId
    ) {
        return parliamentaryService.findTable(tableId, memberId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/table/parliamentary/{tableId}")
    public void deleteTable(
            @PathVariable Long tableId,
            @RequestParam Long memberId
    ) {
        parliamentaryService.deleteTable(tableId, memberId);
    }


}
