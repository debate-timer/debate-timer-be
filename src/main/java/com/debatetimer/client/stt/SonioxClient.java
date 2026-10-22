package com.debatetimer.client.stt;

import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Soniox 비동기 전사 API로 오디오 한 개를 전사한다.
 * 파일 업로드 → 전사 생성 → 완료까지 상태 조회 → 토큰 조회 순으로 진행하고, 끝나면 Soniox에 남은 파일과 전사를 지운다.
 */
@Slf4j
@Component
@EnableConfigurationProperties(SonioxProperties.class)
public class SonioxClient {

    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_ERROR = "error";
    private static final String TRANSLATION_TOKEN = "translation";

    private final RestClient restClient;
    private final SonioxProperties properties;

    @Autowired
    public SonioxClient(RestClient.Builder restClientBuilder, SonioxProperties properties) {
        this(createRestClient(restClientBuilder, properties), properties);
    }

    SonioxClient(RestClient restClient, SonioxProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    private static RestClient createRestClient(RestClient.Builder restClientBuilder, SonioxProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(properties.timeout());
        return restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public List<SpeechToken> transcribe(Resource audio, List<String> languageHints) {
        if (!properties.hasApiKey()) {
            throw new DTServerErrorException(ServerErrorCode.STT_NOT_CONFIGURED);
        }

        String fileId = null;
        String transcriptionId = null;
        try {
            fileId = uploadFile(audio);
            transcriptionId = createTranscription(fileId, languageHints);
            waitUntilCompleted(transcriptionId);
            return fetchTokens(transcriptionId);
        } catch (RestClientException exception) {
            log.warn("Soniox 전사 요청에 실패했습니다. fileId={}, transcriptionId={}", fileId, transcriptionId, exception);
            throw new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED);
        } finally {
            cleanUp(fileId, transcriptionId);
        }
    }

    private String uploadFile(Resource audio) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audio);
        IdResponse response = restClient.post()
                .uri("/files")
                .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(IdResponse.class);
        return requireId(response);
    }

    private String createTranscription(String fileId, List<String> languageHints) {
        Map<String, Object> body = Map.of(
                "model", properties.model(),
                "file_id", fileId,
                "language_hints", languageHints
        );
        IdResponse response = restClient.post()
                .uri("/transcriptions")
                .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(IdResponse.class);
        return requireId(response);
    }

    private void waitUntilCompleted(String transcriptionId) {
        long deadline = System.nanoTime() + properties.maxWait().toNanos();
        while (true) {
            TranscriptionResponse transcription = restClient.get()
                    .uri("/transcriptions/{id}", transcriptionId)
                    .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                    .retrieve()
                    .body(TranscriptionResponse.class);
            String status = transcription == null ? null : transcription.status();
            if (STATUS_COMPLETED.equals(status)) {
                return;
            }
            if (STATUS_ERROR.equals(status)) {
                log.warn("Soniox 전사가 실패했습니다. transcriptionId={}, error={}", transcriptionId,
                        transcription.errorMessage());
                throw new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED);
            }
            if (System.nanoTime() >= deadline) {
                log.warn("Soniox 전사가 제한 시간 안에 끝나지 않았습니다. transcriptionId={}", transcriptionId);
                throw new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED);
            }
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(properties.pollInterval().toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED);
        }
    }

    private List<SpeechToken> fetchTokens(String transcriptionId) {
        TranscriptResponse transcript = restClient.get()
                .uri("/transcriptions/{id}/transcript", transcriptionId)
                .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                .retrieve()
                .body(TranscriptResponse.class);
        if (transcript == null || transcript.tokens() == null) {
            return List.of();
        }
        return transcript.tokens().stream()
                .filter(token -> !TRANSLATION_TOKEN.equals(token.translationStatus()))
                .map(token -> new SpeechToken(token.text(), token.startMs(), token.endMs()))
                .toList();
    }

    // 정리 실패는 전사 결과에 영향을 주지 않으므로 기록만 남긴다.
    private void cleanUp(@Nullable String fileId, @Nullable String transcriptionId) {
        if (transcriptionId != null) {
            delete("/transcriptions/{id}", transcriptionId);
        }
        if (fileId != null) {
            delete("/files/{id}", fileId);
        }
    }

    private void delete(String uri, String id) {
        try {
            restClient.delete()
                    .uri(uri, id)
                    .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Soniox 리소스를 지우지 못했습니다. uri={}, id={}", uri, id, exception);
        }
    }

    private String requireId(@Nullable IdResponse response) {
        if (response == null || response.id() == null) {
            throw new DTServerErrorException(ServerErrorCode.STT_REQUEST_FAILED);
        }
        return response.id();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IdResponse(String id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TranscriptionResponse(String status, @JsonProperty("error_message") String errorMessage) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TranscriptResponse(List<TokenResponse> tokens) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(
            String text,
            @JsonProperty("start_ms") long startMs,
            @JsonProperty("end_ms") long endMs,
            @JsonProperty("translation_status") String translationStatus
    ) {
    }
}
