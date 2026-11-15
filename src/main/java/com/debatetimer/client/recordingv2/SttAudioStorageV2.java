package com.debatetimer.client.recordingv2;

import com.debatetimer.client.storage.SttAudioStorageProperties;
import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 턴마다 고른 후보 오디오를 S3에 보관한다. V1과 같은 버킷을 쓰되 경로 접두사를 나눠 섞이지 않게 한다.
 * 이미 백그라운드 스레드에서 호출되므로 바로 올리고, 실패하면 기록만 남기고 전사는 계속한다.
 */
@Slf4j
public class SttAudioStorageV2 {

    private static final String KEY_PREFIX_SUFFIX = "-v2";

    // 꺼져 있거나 설정이 비어 있으면 null이고, 저장하지 않는다.
    @Nullable
    private final S3Client s3Client;
    private final SttAudioStorageProperties properties;

    public SttAudioStorageV2(@Nullable S3Client s3Client, SttAudioStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    /**
     * @return 저장한 S3 키. 저장하지 않았거나 실패하면 비어 있다.
     */
    public Optional<String> upload(long memberId, String sessionKey, String turnKey, AudioCandidateV2 candidate) {
        if (s3Client == null) {
            return Optional.empty();
        }
        String key = createKey(memberId, sessionKey, turnKey, candidate);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(candidate.contentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromFile(candidate.file()));
            return Optional.of(key);
        } catch (RuntimeException exception) {
            log.warn("V2 녹음 오디오를 S3에 저장하지 못했습니다. key={}, size={}", key, candidate.sizeBytes(), exception);
            return Optional.empty();
        }
    }

    // {prefix}-v2/{memberId}/{sessionKey}/{turnKey}-{deviceId}.{확장자}
    private String createKey(long memberId, String sessionKey, String turnKey, AudioCandidateV2 candidate) {
        String extension = StringUtils.getFilenameExtension(candidate.file().getFileName().toString());
        String fileName = turnKey + "-" + candidate.deviceId() + (StringUtils.hasText(extension) ? "." + extension : "");
        return String.join("/", properties.keyPrefix() + KEY_PREFIX_SUFFIX, String.valueOf(memberId), sessionKey,
                fileName);
    }

    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
