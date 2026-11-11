package com.debatetimer.controller.transcript;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.response.SharedTranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptShareResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponses;
import com.debatetimer.service.transcript.TranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
            @AuthMember(requires = FeatureFlag.TRANSCRIPT) Member member,
            @PathVariable(name = "tableId") long tableId,
            @RequestBody @Valid TranscriptCreateRequest request
    ) {
        return transcriptService.create(tableId, member, request);
    }

    @GetMapping("/api/transcripts")
    @ResponseStatus(HttpStatus.OK)
    public TranscriptSummaryResponses getTranscripts(@AuthMember(requires = FeatureFlag.TRANSCRIPT) Member member) {
        return transcriptService.findAll(member);
    }

    @GetMapping("/api/transcripts/{transcriptId}")
    @ResponseStatus(HttpStatus.OK)
    public TranscriptResponse getTranscript(
            @AuthMember(requires = FeatureFlag.TRANSCRIPT) Member member,
            @PathVariable(name = "transcriptId") long transcriptId
    ) {
        return transcriptService.get(transcriptId, member);
    }

    @PostMapping("/api/transcripts/{transcriptId}/share")
    @ResponseStatus(HttpStatus.OK)
    public TranscriptShareResponse shareTranscript(
            @AuthMember(requires = FeatureFlag.TRANSCRIPT) Member member,
            @PathVariable(name = "transcriptId") long transcriptId
    ) {
        return transcriptService.share(transcriptId, member);
    }

    @DeleteMapping("/api/transcripts/{transcriptId}/share")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unshareTranscript(
            @AuthMember(requires = FeatureFlag.TRANSCRIPT) Member member,
            @PathVariable(name = "transcriptId") long transcriptId
    ) {
        transcriptService.unshare(transcriptId, member);
    }

    // 공유 링크를 받은 사람은 로그인하지 않고 볼 수 있다.
    @GetMapping("/api/shared-transcripts/{shareToken}")
    @ResponseStatus(HttpStatus.OK)
    public SharedTranscriptResponse getSharedTranscript(@PathVariable(name = "shareToken") String shareToken) {
        return transcriptService.getShared(shareToken);
    }
}
