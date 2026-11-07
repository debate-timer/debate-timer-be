package com.debatetimer.client.storage;

import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 토론 기록 Soniox 모드로 올라온 발언 구간 오디오를 나중에 다시 들을 수 있도록 S3에 보관한다.
 * Soniox는 업로드한 파일을 다시 내려받는 API가 없어 직접 보관한다.
 * 보관은 부가 기능이라 전사 응답을 기다리게 하지 않도록 별도 스레드에서 올리고, 실패해도 기록만 남긴다.
 * 빈은 {@link com.debatetimer.config.StorageConfig}에서 설정에 따라 만든다.
 */
@Slf4j
@RequiredArgsConstructor
public class SttAudioStorage {

    // 자정을 넘기는 토론도 세션 안에서 녹음 순서대로 정렬되도록 날짜까지 붙인다.
    private static final DateTimeFormatter TIME_PREFIX = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    // 꺼져 있거나 설정이 비어 있으면 null이고, 저장하지 않는다.
    @Nullable
    private final S3Client s3Client;
    private final SttAudioStorageProperties properties;
    private final Clock clock;

    public boolean isEnabled() {
        return s3Client != null;
    }

    @Async
    public void save(SttAudio audio) {
        if (s3Client == null) {
            return;
        }
        String key = createKey(audio);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(audio.contentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(audio.content()));
        } catch (SdkException exception) {
            log.warn("STT 오디오를 S3에 저장하지 못했습니다. key={}, size={}", key, audio.content().length, exception);
        }
    }

    // 회원·토론 세션별로 모아 보기 쉽게 {prefix}/{memberId}/{sessionId}/{yyyyMMdd-HHmmss}-{uuid}.{확장자} 로 저장한다.
    private String createKey(SttAudio audio) {
        LocalDateTime now = LocalDateTime.now(clock);
        String extension = StringUtils.getFilenameExtension(audio.originalFilename());
        String fileName = now.format(TIME_PREFIX) + "-" + UUID.randomUUID()
                + (StringUtils.hasText(extension) ? "." + extension : "");
        return String.join("/", properties.keyPrefix(), String.valueOf(audio.memberId()), audio.sessionId(), fileName);
    }

    @PreDestroy
    void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
