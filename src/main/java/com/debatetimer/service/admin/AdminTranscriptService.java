package com.debatetimer.service.admin;

import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.dto.admin.response.AdminTranscriptDetailResponse;
import com.debatetimer.dto.admin.response.AdminTranscriptPageResponse;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.event.transcript.TranscriptCreatedEvent;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민이 모든 회원의 토론 기록을 소유자 확인 없이 조회하고, 프롬프트 검증용으로 보정·분석을 다시 돌린다.
 */
@Service
@RequiredArgsConstructor
public class AdminTranscriptService {

    private final TranscriptDomainRepository transcriptDomainRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public AdminTranscriptPageResponse getTranscripts(int page, int size) {
        if (page < 0 || size <= 0 || size > AdminTableService.MAX_PAGE_SIZE) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_PAGE_REQUEST);
        }
        return AdminTranscriptPageResponse.from(transcriptDomainRepository.findOverviews(page, size));
    }

    public AdminTranscriptDetailResponse getTranscript(long transcriptId) {
        Transcript transcript = transcriptDomainRepository.getById(transcriptId);
        return new AdminTranscriptDetailResponse(transcript, readReport(transcript.reportJson()));
    }

    // 원본과 같은 회원·시간표 스냅샷·STT 모드로 새 기록을 만들고, 커밋 뒤 현재 프롬프트로 보정·분석을 돌린다.
    @Transactional
    public TranscriptCreateResponse rerun(long transcriptId) {
        Transcript source = transcriptDomainRepository.getById(transcriptId);
        Transcript rerun = transcriptDomainRepository.createRerun(source);
        eventPublisher.publishEvent(new TranscriptCreatedEvent(rerun.id()));
        return new TranscriptCreateResponse(rerun.id());
    }

    // 어드민은 보고서가 깨져 있어도 확인할 수 있어야 하므로 읽지 못하면 문자열 그대로 준다.
    private JsonNode readReport(String reportJson) {
        if (reportJson == null) {
            return null;
        }
        try {
            return objectMapper.readTree(reportJson);
        } catch (JsonProcessingException exception) {
            return TextNode.valueOf(reportJson);
        }
    }
}
