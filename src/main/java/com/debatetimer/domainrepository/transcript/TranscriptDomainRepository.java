package com.debatetimer.domainrepository.transcript;

import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptSummary;
import com.debatetimer.entity.transcript.TranscriptEntity;
import com.debatetimer.repository.transcript.TranscriptRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TranscriptDomainRepository {

    private static final TypeReference<List<TranscriptLine>> LINES_TYPE = new TypeReference<>() {
    };

    private final TranscriptRepository transcriptRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Transcript create(long tableId, long memberId, SttMode sttMode, List<TranscriptLine> lines) {
        TranscriptEntity entity = new TranscriptEntity(tableId, memberId, sttMode, writeLines(lines));
        return toDomain(transcriptRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Transcript getById(long id) {
        return toDomain(transcriptRepository.getById(id));
    }

    @Transactional(readOnly = true)
    public Transcript getByIdAndMemberId(long id, long memberId) {
        return toDomain(transcriptRepository.getByIdAndMemberId(id, memberId));
    }

    @Transactional(readOnly = true)
    public List<TranscriptSummary> findSummariesByMemberId(long memberId) {
        return transcriptRepository.findSummariesByMemberId(memberId);
    }

    @Transactional
    public void refine(long id, List<TranscriptLine> refinedLines) {
        transcriptRepository.getById(id).refine(writeLines(refinedLines));
    }

    @Transactional
    public void skipRefinement(long id) {
        transcriptRepository.getById(id).skipRefinement();
    }

    @Transactional
    public void complete(long id, String reportJson) {
        transcriptRepository.getById(id).complete(reportJson);
    }

    @Transactional
    public void fail(long id, String errorMessage) {
        transcriptRepository.getById(id).fail(errorMessage);
    }

    private Transcript toDomain(TranscriptEntity entity) {
        return new Transcript(entity.getId(), entity.getTableId(), entity.getMemberId(), entity.getStatus(),
                entity.getSttMode(), entity.isRefining(), readLines(entity.getLinesJson()),
                readRawLines(entity.getRawLinesJson()), entity.getReportJson(),
                entity.getErrorMessage());
    }

    private String writeLines(List<TranscriptLine> lines) {
        try {
            return objectMapper.writeValueAsString(lines);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("속기록을 저장 형식으로 변환하지 못했습니다.", exception);
        }
    }

    // 다듬기 도입 전 속기록은 원문을 따로 보관하지 않았다.
    private List<TranscriptLine> readRawLines(String rawLinesJson) {
        return rawLinesJson == null ? List.of() : readLines(rawLinesJson);
    }

    private List<TranscriptLine> readLines(String linesJson) {
        try {
            return objectMapper.readValue(linesJson, LINES_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 속기록을 읽지 못했습니다.", exception);
        }
    }
}
