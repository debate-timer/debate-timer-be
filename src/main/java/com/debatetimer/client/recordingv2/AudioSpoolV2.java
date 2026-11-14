package com.debatetimer.client.recordingv2;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 턴 후보 오디오를 고를 때까지 로컬 임시 파일로 받아 둔다. 기기 수만큼 늘어나는 오디오를 힙에 올리지 않기 위해서다.
 * 받아 둔 bytes 합계가 상한을 넘으면 새 오디오를 받지 않는다.
 */
@Slf4j
public class AudioSpoolV2 {

    private final Path directory;
    private final long maxBytes;
    private final AtomicLong usedBytes = new AtomicLong();

    public AudioSpoolV2(Path directory, long maxBytes) {
        this.directory = directory;
        this.maxBytes = maxBytes;
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new UncheckedIOException("녹음 스풀 디렉터리를 만들지 못했습니다. dir=" + directory, exception);
        }
        // 재시작 전에 남은 파일도 정리할 때 용량을 돌려받으므로 처음부터 사용량에 넣는다.
        usedBytes.set(sizeOfExistingFiles());
    }

    public Path save(MultipartFile audio) {
        long size = audio.getSize();
        if (usedBytes.addAndGet(size) > maxBytes) {
            usedBytes.addAndGet(-size);
            log.warn("녹음 스풀 용량이 가득 차 오디오를 받지 않습니다. size={}", size);
            throw new DTClientErrorException(ClientErrorCode.RECORDING_BUSY);
        }
        Path file = directory.resolve(createFileName(audio.getOriginalFilename()));
        try (InputStream inputStream = audio.getInputStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException | RuntimeException exception) {
            usedBytes.addAndGet(-size);
            deleteQuietly(file);
            throw new IllegalStateException("녹음 오디오를 임시 파일로 받지 못했습니다.", exception);
        }
    }

    public void delete(Path file) {
        try {
            long size = Files.size(file);
            if (Files.deleteIfExists(file)) {
                usedBytes.addAndGet(-size);
            }
        } catch (IOException exception) {
            log.warn("녹음 임시 파일을 지우지 못했습니다. file={}", file, exception);
        }
    }

    /**
     * 재시작 등으로 주인을 잃은 임시 파일을 지운다. 무중단 배포 중 다른 앱이 쓰는 파일을 지우지 않도록 오래된 파일만 지운다.
     */
    public void deleteOlderThan(Duration age, Clock clock) {
        Instant threshold = clock.instant().minus(age);
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(file -> isOlderThan(file, threshold))
                    .forEach(this::delete);
        } catch (IOException exception) {
            log.warn("오래된 녹음 임시 파일을 정리하지 못했습니다. dir={}", directory, exception);
        }
    }

    long getUsedBytes() {
        return usedBytes.get();
    }

    private long sizeOfExistingFiles() {
        try (Stream<Path> files = Files.list(directory)) {
            return files.mapToLong(this::sizeOf).sum();
        } catch (IOException exception) {
            log.warn("녹음 스풀 디렉터리를 읽지 못했습니다. dir={}", directory, exception);
            return 0;
        }
    }

    private long sizeOf(Path file) {
        try {
            return Files.size(file);
        } catch (IOException exception) {
            return 0;
        }
    }

    private boolean isOlderThan(Path file, Instant threshold) {
        try {
            return Files.getLastModifiedTime(file).toInstant().isBefore(threshold);
        } catch (IOException exception) {
            return false;
        }
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            log.warn("녹음 임시 파일을 지우지 못했습니다. file={}", file, exception);
        }
    }

    private String createFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String name = UUID.randomUUID().toString();
        return isSafeExtension(extension) ? name + "." + extension : name;
    }

    // 확장자는 Soniox가 형식을 알아보는 데 쓰므로 남기되, 경로 문자가 섞인 값은 버린다.
    private boolean isSafeExtension(String extension) {
        return extension != null && extension.matches("[A-Za-z0-9]{1,10}");
    }
}
