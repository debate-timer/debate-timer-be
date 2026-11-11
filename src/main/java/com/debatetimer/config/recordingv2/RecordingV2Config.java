package com.debatetimer.config.recordingv2;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.client.recordingv2.SpeechTranscriberV2;
import com.debatetimer.client.recordingv2.SttAudioStorageV2;
import com.debatetimer.client.storage.SttAudioStorageProperties;
import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.client.stt.SonioxProperties;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * 다기기 녹음 V2 전용 부품. V1 빈과 섞이지 않도록 S3 클라이언트와 SonioxClient는 빈으로 등록하지 않고 V2 부품 안에 둔다.
 * (SonioxClient를 빈으로 하나 더 만들면 V1 주입이 모호해진다.)
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RecordingV2Properties.class)
public class RecordingV2Config {

    @Bean
    public AudioSpoolV2 audioSpoolV2(RecordingV2Properties properties) {
        return new AudioSpoolV2(Path.of(properties.spoolDir()), properties.maxSpoolBytes());
    }

    @Bean(destroyMethod = "close")
    public SttAudioStorageV2 sttAudioStorageV2(SttAudioStorageProperties properties) {
        return new SttAudioStorageV2(createS3Client(properties), properties);
    }

    // 전사는 V2 작업 스레드에서만 하므로 동시 전사 수를 작업 스레드 수에 맞춘다.
    @Bean
    public SpeechTranscriberV2 speechTranscriberV2(RestClient.Builder restClientBuilder,
                                                   SonioxProperties sonioxProperties,
                                                   RecordingV2Properties properties) {
        SonioxProperties v2Properties = new SonioxProperties(sonioxProperties.apiKey(), sonioxProperties.model(),
                sonioxProperties.baseUrl(), sonioxProperties.timeout(), sonioxProperties.pollInterval(),
                sonioxProperties.maxWait(), sonioxProperties.connectTimeout(), properties.workers());
        return new SpeechTranscriberV2(new SonioxClient(restClientBuilder, v2Properties));
    }

    @Nullable
    private S3Client createS3Client(SttAudioStorageProperties properties) {
        if (!properties.enabled() || !properties.isComplete()) {
            return null;
        }
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .build();
    }
}
