package com.debatetimer.client.stt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Soniox 비동기 전사 설정. 키가 비어 있어도 앱은 뜨고, Soniox 모드 전사 요청만 실패한다.
 */
@ConfigurationProperties(prefix = "soniox")
public record SonioxProperties(
        String apiKey,
        String model,
        String baseUrl,
        Duration timeout,
        Duration pollInterval,
        Duration maxWait
) {

    private static final String DEFAULT_MODEL = "stt-async-v5";
    private static final String DEFAULT_BASE_URL = "https://api.soniox.com/v1";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
    private static final Duration DEFAULT_MAX_WAIT = Duration.ofSeconds(180);

    public SonioxProperties {
        model = isBlank(model) ? DEFAULT_MODEL : model;
        baseUrl = isBlank(baseUrl) ? DEFAULT_BASE_URL : baseUrl;
        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
        pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        maxWait = maxWait == null ? DEFAULT_MAX_WAIT : maxWait;
    }

    public boolean hasApiKey() {
        return !isBlank(apiKey);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
