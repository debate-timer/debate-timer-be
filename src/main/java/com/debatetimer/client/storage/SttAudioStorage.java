package com.debatetimer.client.storage;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 토론 기록 Soniox 모드로 올라온 발언 구간 오디오를 나중에 다시 들을 수 있도록 S3에 보관한다.
 * Soniox는 업로드한 파일을 다시 내려받는 API가 없어 직접 보관한다. 보관은 부가 기능이라 실패해도 전사는 계속한다.
 */
@Slf4j
@Component
@EnableConfigurationProperties(SttAudioStorageProperties.class)
public class SttAudioStorage {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_PREFIX = DateTimeFormatter.ofPattern("HHmmss");

    // 꺼져 있거나 설정이 비어 있으면 null이고, 저장하지 않는다.
    @Nullable
    private final S3Client s3Client;
    private final SttAudioStorageProperties properties;
    private final Clock clock;

    @Autowired
    public SttAudioStorage(SttAudioStorageProperties properties) {
        this(createS3Client(properties), properties, Clock.system(KST));
    }

    SttAudioStorage(@Nullable S3Client s3Client, SttAudioStorageProperties properties, Clock clock) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.clock = clock;
    }

    @Nullable
    private static S3Client createS3Client(SttAudioStorageProperties properties) {
        if (!properties.enabled()) {
            return null;
        }
        if (!properties.isComplete()) {
            log.warn("STT 오디오 S3 보관이 켜져 있지만 버킷·리전·키 설정이 비어 있어 저장하지 않습니다.");
            return null;
        }
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .build();
    }

    public void save(long memberId, MultipartFile audio) {
        if (s3Client == null) {
            return;
        }
        String key = createKey(memberId, audio.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(audio.getContentType())
                .build();
        try (InputStream content = audio.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(content, audio.getSize()));
        } catch (IOException | SdkException exception) {
            log.warn("STT 오디오를 S3에 저장하지 못했습니다. key={}, size={}", key, audio.getSize(), exception);
        }
    }

    // 날짜·회원별로 모아 보기 쉽게 {prefix}/yyyy/MM/dd/member-{id}/{HHmmss}-{uuid}.{확장자} 로 저장한다.
    private String createKey(long memberId, @Nullable String originalFilename) {
        LocalDateTime now = LocalDateTime.now(clock);
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String fileName = now.format(TIME_PREFIX) + "-" + UUID.randomUUID()
                + (StringUtils.hasText(extension) ? "." + extension : "");
        return String.join("/", properties.keyPrefix(), now.format(DATE_PATH), "member-" + memberId, fileName);
    }

    @PreDestroy
    void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
