package com.debatetimer.client.llm;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 분석 보고서 생성에 쓰는 OpenAI 설정. 키가 비어 있어도 앱은 뜨고, 분석만 실패로 기록한다.
 */
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(String apiKey, String model, String baseUrl, Duration timeout) {

    private static final String DEFAULT_MODEL = "gpt-5-mini";
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(180);

    public OpenAiProperties {
        model = isBlank(model) ? DEFAULT_MODEL : model;
        baseUrl = isBlank(baseUrl) ? DEFAULT_BASE_URL : baseUrl;
        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
    }

    public boolean hasApiKey() {
        return !isBlank(apiKey);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
