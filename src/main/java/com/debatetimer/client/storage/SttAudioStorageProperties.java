package com.debatetimer.client.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 토론 기록 Soniox 모드 발언 구간 오디오의 S3 보관 설정. 꺼져 있거나 값이 비어 있어도 앱은 뜨고, 저장만 하지 않는다.
 */
@ConfigurationProperties(prefix = "stt-audio")
public record SttAudioStorageProperties(
        Boolean enabled,
        String bucket,
        String region,
        String accessKey,
        String secretKey,
        String keyPrefix
) {

    private static final String DEFAULT_KEY_PREFIX = "stt-audio";

    public SttAudioStorageProperties {
        enabled = enabled != null && enabled;
        keyPrefix = isBlank(keyPrefix) ? DEFAULT_KEY_PREFIX : keyPrefix;
    }

    public boolean isComplete() {
        return !isBlank(bucket) && !isBlank(region) && !isBlank(accessKey) && !isBlank(secretKey);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
