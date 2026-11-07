package com.debatetimer.client.storage;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
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
    // 프론트는 토론 시작 시 UUID로 만든다.
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");
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

    /**
     * 발언 구간 오디오를 회원·토론 세션별 경로로 보관하도록 맡긴다. 보관할 수 없으면 기록만 남기고 넘어가므로 전사에는 영향이 없다.
     * 업로드는 요청이 끝난 뒤에 이뤄지므로 multipart 임시 파일 대신 읽어 둔 bytes를 넘긴다.
     */
    public void save(long memberId, @Nullable String sessionId, MultipartFile audio) {
        // 보관은 부가 기능이라 예상하지 못한 예외도 전사 요청으로 넘기지 않는다.
        try {
            saveIfPossible(memberId, sessionId, audio);
        } catch (RuntimeException exception) {
            log.warn("STT 오디오 보관 중 예상하지 못한 오류가 나 보관하지 않습니다. memberId={}, sessionId={}",
                    memberId, sessionId, exception);
        }
    }

    private void saveIfPossible(long memberId, @Nullable String sessionId, MultipartFile audio) {
        if (s3Client == null || !isValidSessionId(memberId, sessionId)) {
            return;
        }
        long size = audio.getSize();
        // 요청 스레드에서 bytes를 읽기 전에 대기 용량부터 잡아, 밀려 있을 때는 큰 오디오를 힙에 올리지 않는다.
        if (pendingBytes.addAndGet(size) > maxPendingBytes) {
            pendingBytes.addAndGet(-size);
            log.warn("업로드를 기다리는 STT 오디오가 많아 보관하지 않습니다. memberId={}, sessionId={}, size={}",
                    memberId, sessionId, size);
            return;
        }
        try {
            SttAudio sttAudio = new SttAudio(memberId, sessionId, audio.getBytes(), audio.getContentType(),
                    audio.getOriginalFilename());
            submit(sttAudio, size);
        } catch (IOException exception) {
            pendingBytes.addAndGet(-size);
            log.warn("STT 오디오를 읽지 못해 보관하지 않습니다. memberId={}, sessionId={}", memberId, sessionId, exception);
        } catch (RuntimeException exception) {
            pendingBytes.addAndGet(-size);
            throw exception;
        }
    }

    // 세션 식별자는 S3 경로에 그대로 들어가므로 경로 문자가 섞인 값은 보관하지 않는다.
    // 세션 식별자를 아직 보내지 않는 프론트 요청은 조용히 건너뛴다.
    private boolean isValidSessionId(long memberId, @Nullable String sessionId) {
        if (sessionId == null) {
            return false;
        }
        if (!SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            log.warn("토론 세션 식별자 형식이 올바르지 않아 STT 오디오를 보관하지 않습니다. memberId={}", memberId);
            return false;
        }
        return true;
    }

    private void submit(SttAudio audio, long size) {
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
        // 업로드 스레드에서 난 예외는 기록만 남긴다. SDK 예외 외의 오류로 스레드가 죽지 않게 함께 잡는다.
        try {
            s3Client.putObject(request, RequestBody.fromBytes(audio.content()));
        } catch (RuntimeException exception) {
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
