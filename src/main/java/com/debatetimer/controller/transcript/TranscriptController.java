package com.debatetimer.controller.transcript;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.service.transcript.TranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    @PostMapping("/api/transcripts/{tableId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TranscriptCreateResponse createTranscript(
            @AuthMember Member member,
            @PathVariable(name = "tableId") long tableId,
            @RequestBody @Valid TranscriptCreateRequest request
    ) {
        return transcriptService.create(tableId, member, request);
    }

    @GetMapping("/api/transcripts/{transcriptId}")
    @ResponseStatus(HttpStatus.OK)
    public TranscriptResponse getTranscript(
            @AuthMember Member member,
            @PathVariable(name = "transcriptId") long transcriptId
    ) {
        return transcriptService.get(transcriptId, member);
    }
}
