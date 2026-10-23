package com.debatetimer.service.transcript;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.transcript.SpeakerSeparator;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponses;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.debatetimer.event.transcript.TranscriptCreatedEvent;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptService {

    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final TranscriptDomainRepository transcriptDomainRepository;
    private final CustomizeTableRepository customizeTableRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final SpeakerSeparator speakerSeparator = new SpeakerSeparator();

    @Transactional
    public TranscriptCreateResponse create(long tableId, Member member, TranscriptCreateRequest request) {
        CustomizeTable table = customizeTableDomainRepository.getByIdAndMember(tableId, member);
        List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getMemberCustomizeTimeBoxes(tableId,
                member);
        List<TranscriptLine> lines = speakerSeparator.separate(timeBoxes, request.toSegments());
        if (lines.isEmpty()) {
            throw new DTClientErrorException(ClientErrorCode.EMPTY_TRANSCRIPT);
        }

        Transcript transcript = transcriptDomainRepository.create(table.getId(), member.getId(),
                request.resolveSttMode(), lines);
        eventPublisher.publishEvent(new TranscriptCreatedEvent(transcript.id()));
        return new TranscriptCreateResponse(transcript.id());
    }

    @Transactional(readOnly = true)
    public TranscriptResponse get(long transcriptId, Member member) {
        Transcript transcript = transcriptDomainRepository.getByIdAndMemberId(transcriptId, member.getId());
        CustomizeTable table = customizeTableDomainRepository.getById(transcript.tableId());
        return new TranscriptResponse(transcript, table, readReport(transcript));
    }

    @Transactional(readOnly = true)
    public TranscriptSummaryResponses findAll(Member member) {
        Map<Long, CustomizeTable> tables = customizeTableRepository.findAllByMember(member).stream()
                .map(CustomizeTableEntity::toDomain)
                .collect(Collectors.toMap(CustomizeTable::getId, Function.identity()));
        List<TranscriptSummaryResponse> transcripts = transcriptDomainRepository.findSummariesByMemberId(member.getId())
                .stream()
                .map(summary -> new TranscriptSummaryResponse(summary, tables.get(summary.tableId())))
                .toList();
        return new TranscriptSummaryResponses(transcripts);
    }

    private JsonNode readReport(Transcript transcript) {
        if (transcript.reportJson() == null) {
            return null;
        }
        try {
            return objectMapper.readTree(transcript.reportJson());
        } catch (JsonProcessingException exception) {
            log.warn("분석 보고서를 읽지 못했습니다. transcriptId={}", transcript.id(), exception);
            return null;
        }
    }
}
