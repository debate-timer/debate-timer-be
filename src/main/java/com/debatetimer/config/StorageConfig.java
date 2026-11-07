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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
@EnableConfigurationProperties({IconStorageProperties.class, SttAudioStorageProperties.class})
public class StorageConfig {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Bean
    public SttAudioStorage sttAudioStorage(SttAudioStorageProperties properties) {
        return new SttAudioStorage(createS3Client(properties), properties, Clock.system(KST));
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
