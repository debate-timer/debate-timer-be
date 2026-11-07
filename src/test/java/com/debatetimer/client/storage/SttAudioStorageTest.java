package com.debatetimer.client.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class SttAudioStorageTest {

    private static final SttAudioStorageProperties PROPERTIES = new SttAudioStorageProperties(
            true, "debate-bucket", "ap-northeast-2", "access", "secret", null);
    // 2026-11-03 13:04:05 KST
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T04:04:05Z"), ZoneId.of("Asia/Seoul"));
    private static final SttAudio AUDIO = new SttAudio(7L, "session-1", new byte[]{1, 2, 3}, "audio/webm",
            "segment.webm");

    private S3Client s3Client;
    private SttAudioStorage storage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        storage = new SttAudioStorage(s3Client, PROPERTIES, CLOCK);
    }

    @Nested
    class Save {

        @Test
        void 회원_토론_세션별_경로로_오디오를_저장한다() {
            storage.save(AUDIO);

            ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
            ArgumentCaptor<RequestBody> body = ArgumentCaptor.forClass(RequestBody.class);
            verify(s3Client).putObject(request.capture(), body.capture());
            assertAll(
                    () -> assertThat(request.getValue().bucket()).isEqualTo("debate-bucket"),
                    () -> assertThat(request.getValue().key())
                            .matches("stt-audio/7/session-1/20261103-130405-[0-9a-f-]{36}\\.webm"),
                    () -> assertThat(request.getValue().contentType()).isEqualTo("audio/webm"),
                    () -> assertThat(body.getValue().optionalContentLength()).hasValue(3L)
            );
        }

        @Test
        void 파일_이름에_확장자가_없으면_확장자_없이_저장한다() {
            storage.save(new SttAudio(7L, "session-1", new byte[]{1}, "audio/webm", "blob"));

            ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(request.capture(), any(RequestBody.class));
            assertThat(request.getValue().key()).matches("stt-audio/7/session-1/20261103-130405-[0-9a-f-]{36}");
        }

        @Test
        void S3_저장에_실패해도_예외를_던지지_않는다() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(S3Exception.builder().message("denied").build());

            assertThatCode(() -> storage.save(AUDIO)).doesNotThrowAnyException();
        }

        @Test
        void S3_클라이언트가_없으면_꺼진_것으로_보고_저장하지_않는다() {
            SttAudioStorage disabled = new SttAudioStorage(null, PROPERTIES, CLOCK);

            assertAll(
                    () -> assertThat(disabled.isEnabled()).isFalse(),
                    () -> assertThatCode(() -> disabled.save(AUDIO)).doesNotThrowAnyException()
            );
        }
    }
}
