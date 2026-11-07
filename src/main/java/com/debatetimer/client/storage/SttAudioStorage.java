package com.debatetimer.client.storage;

import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 토론 기록 Soniox 모드로 올라온 발언 구간 오디오를 나중에 다시 들을 수 있도록 S3에 보관한다.
 * Soniox는 업로드한 파일을 다시 내려받는 API가 없어 직접 보관한다.
 * 보관은 부가 기능이라 전사 응답을 기다리게 하지 않도록 전용 업로드 스레드에서 올리고, 실패하거나 밀리면 기록만 남기고 건너뛴다.
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
    private final TaskExecutor uploadExecutor;
    // 업로드를 기다리는 오디오 bytes 합계 상한. 오디오는 업로드가 끝날 때까지 힙에 남으므로, 밀려도 힙을 일정 이상 차지하지 않게 한다.
    private final long maxPendingBytes;
    private final AtomicLong pendingBytes = new AtomicLong();

    public boolean isEnabled() {
        return s3Client != null;
    }

    public void save(SttAudio audio) {
        if (s3Client == null) {
            return;
        }
        long size = audio.content().length;
        if (pendingBytes.addAndGet(size) > maxPendingBytes) {
            pendingBytes.addAndGet(-size);
            log.warn("업로드를 기다리는 STT 오디오가 많아 보관하지 않습니다. memberId={}, sessionId={}, size={}",
                    audio.memberId(), audio.sessionId(), size);
            return;
        }
        // 녹음 순서대로 정렬되도록 경로는 업로드 시점이 아니라 요청 시점으로 정한다.
        String key = createKey(audio);
        try {
            uploadExecutor.execute(() -> {
                try {
                    upload(key, audio);
                } finally {
                    pendingBytes.addAndGet(-size);
                }
            });
        } catch (TaskRejectedException exception) {
            pendingBytes.addAndGet(-size);
            log.warn("STT 오디오 업로드 대기열이 가득 차 보관하지 않습니다. key={}, size={}", key, size);
        }
    }

    private void upload(String key, SttAudio audio) {
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

    // 대기 중인 업로드를 마친 뒤 S3 클라이언트를 닫는다.
    @PreDestroy
    void close() {
        if (uploadExecutor instanceof ThreadPoolTaskExecutor executor) {
            executor.shutdown();
        }
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
