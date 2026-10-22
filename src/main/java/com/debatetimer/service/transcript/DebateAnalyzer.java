package com.debatetimer.service.transcript;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * 화자가 구분된 속기록을 OpenAI에 보내 분석 보고서(주장·근거, 충돌 쟁점, 핵심 발언, 팀별 피드백)를 만든다.
 */
@Slf4j
@Service
public class DebateAnalyzer {

    private static final String SCHEMA_NAME = "debate_analysis_report";
    private static final TypeReference<Map<String, Object>> SCHEMA_TYPE = new TypeReference<>() {
    };

    private final OpenAiClient openAiClient;
    private final TranscriptDomainRepository transcriptDomainRepository;
    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final String systemPrompt;
    private final Map<String, Object> schema;

    public DebateAnalyzer(
            OpenAiClient openAiClient,
            TranscriptDomainRepository transcriptDomainRepository,
            CustomizeTableDomainRepository customizeTableDomainRepository,
            ObjectMapper objectMapper
    ) {
        this.openAiClient = openAiClient;
        this.transcriptDomainRepository = transcriptDomainRepository;
        this.customizeTableDomainRepository = customizeTableDomainRepository;
        this.systemPrompt = readResource("prompt/debate-analysis-system.txt");
        this.schema = readSchema(objectMapper);
    }

    public void analyze(long transcriptId) {
        try {
            Transcript transcript = transcriptDomainRepository.getById(transcriptId);
            CustomizeTable table = customizeTableDomainRepository.getById(transcript.tableId());
            String report = openAiClient.requestStructuredOutput(systemPrompt, buildUserPrompt(table, transcript),
                    SCHEMA_NAME, schema);
            transcriptDomainRepository.complete(transcriptId, report);
        } catch (RuntimeException exception) {
            log.warn("토론 분석에 실패했습니다. transcriptId={}", transcriptId, exception);
            transcriptDomainRepository.fail(transcriptId, exception.getMessage());
        }
    }

    private String buildUserPrompt(CustomizeTable table, Transcript transcript) {
        String lines = transcript.lines().stream()
                .map(this::formatLine)
                .collect(Collectors.joining("\n"));
        return """
                토론 주제: %s
                찬성 팀: %s
                반대 팀: %s
                
                속기록:
                %s
                """.formatted(table.getAgenda(), table.getProsTeamName(), table.getConsTeamName(), lines);
    }

    private String formatLine(TranscriptLine line) {
        String speaker = line.speaker() == null || line.speaker().isBlank() ? "" : " " + line.speaker();
        return "[%d] (%s) %s | %s%s: %s".formatted(line.index(), formatOffset(line.offsetMs()), line.speechType(),
                stanceLabel(line.stance()), speaker, line.text());
    }

    private String formatOffset(long offsetMs) {
        long totalSeconds = offsetMs / 1_000;
        return "%02d:%02d".formatted(totalSeconds / 60, totalSeconds % 60);
    }

    private String stanceLabel(Stance stance) {
        return switch (stance) {
            case PROS -> "찬성";
            case CONS -> "반대";
            case NEUTRAL -> "중립";
        };
    }

    private Map<String, Object> readSchema(ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(readResource("prompt/debate-analysis-schema.json"), SCHEMA_TYPE);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String readResource(String path) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
