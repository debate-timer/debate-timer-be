package com.debatetimer.client.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.debatetimer.client.storage.SttAudioStorageProperties;
import com.debatetimer.domain.recordingv2.AudioCandidateV2;
import com.debatetimer.domain.recordingv2.AudioQualityMetricsV2;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class SttAudioStorageV2Test {

    private static final SttAudioStorageProperties PROPERTIES = new SttAudioStorageProperties(
            true, "debate-bucket", "ap-northeast-2", "access", "secret", null);

    @TempDir
    private Path directory;

    @Test
    void 회원_세션별_V2_경로로_고른_후보를_저장한다() throws IOException {
        S3Client s3Client = mock(S3Client.class);
        SttAudioStorageV2 storage = new SttAudioStorageV2(s3Client, PROPERTIES);

        assertThat(storage.upload(7L, "session-1", "0-3", candidate())).hasValue("stt-audio-v2/7/session-1/0-3-device.webm");

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> body = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(request.capture(), body.capture());
        assertAll(
                () -> assertThat(request.getValue().bucket()).isEqualTo("debate-bucket"),
                () -> assertThat(request.getValue().contentType()).isEqualTo("audio/webm"),
                () -> assertThat(body.getValue().optionalContentLength()).hasValue(3L)
        );
    }

    @Test
    void S3_클라이언트가_없으면_저장하지_않는다() throws IOException {
        SttAudioStorageV2 storage = new SttAudioStorageV2(null, PROPERTIES);

        assertThat(storage.upload(7L, "session-1", "0-3", candidate())).isEmpty();
    }

    @Test
    void 저장에_실패해도_예외를_던지지_않는다() throws IOException {
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("fail").build());
        SttAudioStorageV2 storage = new SttAudioStorageV2(s3Client, PROPERTIES);

        assertThat(storage.upload(7L, "session-1", "0-3", candidate())).isEmpty();
    }

    private AudioCandidateV2 candidate() throws IOException {
        Path file = Files.write(directory.resolve("spooled.webm"), new byte[]{1, 2, 3});
        return new AudioCandidateV2("device", true, file, "audio/webm", "turn.webm", 3, 1_000, 0, 0, null,
                AudioQualityMetricsV2.empty(), Instant.EPOCH);
    }
}
