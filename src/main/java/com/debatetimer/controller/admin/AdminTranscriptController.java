package com.debatetimer.controller.admin;

import com.debatetimer.dto.admin.response.AdminTranscriptDetailResponse;
import com.debatetimer.dto.admin.response.AdminTranscriptPageResponse;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.service.admin.AdminTranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminTranscriptController {

    private final AdminTranscriptService adminTranscriptService;

    @GetMapping("/api/admin/transcripts")
    @ResponseStatus(HttpStatus.OK)
    public AdminTranscriptPageResponse getTranscripts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return adminTranscriptService.getTranscripts(page, size);
    }

    @GetMapping("/api/admin/transcripts/{transcriptId}")
    @ResponseStatus(HttpStatus.OK)
    public AdminTranscriptDetailResponse getTranscript(@PathVariable long transcriptId) {
        return adminTranscriptService.getTranscript(transcriptId);
    }

    @PostMapping("/api/admin/transcripts/{transcriptId}/rerun")
    @ResponseStatus(HttpStatus.CREATED)
    public TranscriptCreateResponse rerun(@PathVariable long transcriptId) {
        return adminTranscriptService.rerun(transcriptId);
    }
}
