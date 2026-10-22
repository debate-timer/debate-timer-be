package com.debatetimer.client.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OpenAI Chat Completions의 Structured Outputs(json_schema, strict)로 구조화된 JSON 응답을 받는다.
 */
@Component
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiClient(RestClient.Builder restClientBuilder, OpenAiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(properties.timeout());
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.properties = properties;
    }

    public String requestStructuredOutput(
            String systemPrompt,
            String userPrompt,
            String schemaName,
            Map<String, Object> schema
    ) {
        if (!properties.hasApiKey()) {
            throw new IllegalStateException("OpenAI API 키가 설정되지 않았습니다. OPENAI_API_KEY를 확인하세요.");
        }

        Map<String, Object> body = Map.of(
                "model", properties.model(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", Map.of(
                        "type", "json_schema",
                        "json_schema", Map.of("name", schemaName, "strict", true, "schema", schema)
                )
        );

        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(ChatCompletionResponse.class);

        return extractContent(response);
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI 응답에 결과가 없습니다.");
        }
        Message message = response.choices().get(0).message();
        if (message.refusal() != null) {
            throw new IllegalStateException("OpenAI가 분석을 거절했습니다: " + message.refusal());
        }
        if (message.content() == null || message.content().isBlank()) {
            throw new IllegalStateException("OpenAI 응답 본문이 비어 있습니다.");
        }
        return message.content();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatCompletionResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Message(String content, String refusal) {
    }
}
