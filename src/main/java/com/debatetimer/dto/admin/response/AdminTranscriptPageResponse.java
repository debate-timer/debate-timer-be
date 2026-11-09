package com.debatetimer.dto.admin.response;

import com.debatetimer.domain.transcript.TranscriptOverview;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminTranscriptPageResponse(
        List<AdminTranscriptResponse> transcripts,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static AdminTranscriptPageResponse from(Page<TranscriptOverview> overviews) {
        return new AdminTranscriptPageResponse(
                overviews.map(AdminTranscriptResponse::new).getContent(),
                overviews.getNumber(),
                overviews.getSize(),
                overviews.getTotalElements(),
                overviews.getTotalPages()
        );
    }
}
