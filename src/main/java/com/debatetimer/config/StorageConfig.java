package com.debatetimer.config;

import com.debatetimer.client.storage.IconStorageProperties;
import com.debatetimer.client.storage.SttAudioStorage;
import com.debatetimer.client.storage.SttAudioStorageProperties;
import java.time.Clock;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
@EnableConfigurationProperties({IconStorageProperties.class, SttAudioStorageProperties.class})
public class StorageConfig {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // STT 오디오 업로드 설정. 1GB 메모리 인스턴스(JVM 기본 최대 힙 약 256MB, 무중단 배포 중에는 앱 두 개)를 기준으로 잡는다.
    // 업로드는 대부분 네트워크 대기라 스레드 하나로 충분하고, CPU를 거의 쓰지 않는다.
    private static final int STT_AUDIO_UPLOAD_THREADS = 1;
    private static final int STT_AUDIO_UPLOAD_QUEUE_CAPACITY = 10;
    // 대기 중인 오디오 bytes 합계 상한. 한 파일 최대 크기(30MB)보다는 커야 긴 발언도 보관할 수 있다.
    private static final long STT_AUDIO_MAX_PENDING_BYTES = 32L * 1024 * 1024;
    private static final int STT_AUDIO_UPLOAD_SHUTDOWN_SECONDS = 20;

    @Bean
    public SttAudioStorage sttAudioStorage(SttAudioStorageProperties properties) {
        return new SttAudioStorage(createS3Client(properties), properties, Clock.system(KST),
                createSttAudioUploadExecutor(), STT_AUDIO_MAX_PENDING_BYTES);
    }

    // Executor 빈으로 등록하면 Spring Boot 기본 TaskExecutor 구성이 빠지므로(AsyncConfig 참고) 빈으로 두지 않고 SttAudioStorage가 닫는다.
    // 대기열이 가득 차면 요청 스레드에서 대신 올리지 않고 거절해 전사 응답을 늦추지 않는다.
    private ThreadPoolTaskExecutor createSttAudioUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(STT_AUDIO_UPLOAD_THREADS);
        executor.setMaxPoolSize(STT_AUDIO_UPLOAD_THREADS);
        executor.setQueueCapacity(STT_AUDIO_UPLOAD_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("stt-audio-upload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(STT_AUDIO_UPLOAD_SHUTDOWN_SECONDS);
        executor.initialize();
        return executor;
    }

    // 꺼져 있거나 설정이 비어 있으면 S3 클라이언트를 만들지 않고, 오디오도 저장하지 않는다.
    @Nullable
    private S3Client createS3Client(SttAudioStorageProperties properties) {
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
}
