package com.debatetimer.service.transcript;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.transcript.RefinedTextMerger;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptTableSnapshot;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * STT 원문 속기록을 OpenAI로 다듬는다. 오인식·문장부호·군말만 고치고 화자와 시각은 그대로 둔다.
 * 다듬기에 실패하면 원문을 속기록으로 쓴다.
 */
@Slf4j
@Service
public class TranscriptRefiner {

    private static final String SCHEMA_NAME = "transcript_refinement";
    private static final TypeReference<Map<String, Object>> SCHEMA_TYPE = new TypeReference<>() {
    };

    private final OpenAiClient openAiClient;
    private final TranscriptDomainRepository transcriptDomainRepository;
    private final ObjectMapper objectMapper;
    private final RefinedTextMerger refinedTextMerger = new RefinedTextMerger();
    private final String systemPrompt;
    private final Map<String, Object> schema;

    public TranscriptRefiner(
            OpenAiClient openAiClient,
            TranscriptDomainRepository transcriptDomainRepository,
            ObjectMapper objectMapper
    ) {
        this.openAiClient = openAiClient;
        this.transcriptDomainRepository = transcriptDomainRepository;
        this.objectMapper = objectMapper;
        this.systemPrompt = readResource("prompt/transcript-refinement-system.txt");
        this.schema = readSchema();
    }

    public void refine(long transcriptId) {
        try {
            Transcript transcript = transcriptDomainRepository.getById(transcriptId);
            String response = openAiClient.requestStructuredOutput(systemPrompt, buildUserPrompt(transcript),
                    SCHEMA_NAME, schema);
            List<TranscriptLine> refined = refinedTextMerger.merge(transcript.lines(), readRefinedTexts(response));
            transcriptDomainRepository.refine(transcriptId, refined);
        } catch (RuntimeException | JsonProcessingException exception) {
            log.warn("속기록 다듬기에 실패해 원문을 씁니다. transcriptId={}", transcriptId, exception);
            transcriptDomainRepository.skipRefinement(transcriptId);
        }
    }

    private String buildUserPrompt(Transcript transcript) {
        TranscriptTableSnapshot table = transcript.table();
        String lines = transcript.lines().stream()
                .map(this::formatLine)
                .collect(Collectors.joining("\n"));
        return """
                토론 주제: %s
                찬성 팀: %s
                반대 팀: %s
                
                속기록:
                %s
                """.formatted(orEmpty(table.agenda()), orEmpty(table.prosTeamName()), orEmpty(table.consTeamName()),
                lines);
    }

    // 속기록을 만들 때 저장한 시간표 정보를 쓰고, 비어 있으면 빈 값으로 둔다.
    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatLine(TranscriptLine line) {
        String speaker = line.speaker() == null || line.speaker().isBlank() ? "" : " " + line.speaker();
        return "[%d] %s | %s%s: %s".formatted(line.index(), line.speechType(), stanceLabel(line.stance()), speaker,
                line.text());
    }

    private String stanceLabel(Stance stance) {
        return switch (stance) {
            case PROS -> "찬성";
            case CONS -> "반대";
            case NEUTRAL -> "중립";
        };
    }

    // 같은 번호가 여러 번 오면 처음 것을 쓴다.
    private Map<Integer, String> readRefinedTexts(String response) throws JsonProcessingException {
        RefinementResponse refinement = objectMapper.readValue(response, RefinementResponse.class);
        if (refinement.lines() == null) {
            return Map.of();
        }
        return refinement.lines().stream()
                .collect(Collectors.toMap(RefinedLine::index, RefinedLine::text, (first, second) -> first));
    }

    private Map<String, Object> readSchema() {
        try {
            return objectMapper.readValue(readResource("prompt/transcript-refinement-schema.json"), SCHEMA_TYPE);
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

    record RefinementResponse(List<RefinedLine> lines) {
    }

    record RefinedLine(int index, String text) {
    }
}
