package com.debatetimer.client.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class SttAudioStorageTest {

    private static final SttAudioStorageProperties PROPERTIES = new SttAudioStorageProperties(
            true, "debate-bucket", "ap-northeast-2", "access", "secret", null);
    // 2026-11-03 13:04:05 KST
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T04:04:05Z"), ZoneId.of("Asia/Seoul"));
    private static final long MEMBER_ID = 7L;
    private static final String SESSION_ID = "session-1";
    private static final MockMultipartFile AUDIO = new MockMultipartFile("audio", "segment.webm", "audio/webm",
            new byte[]{1, 2, 3});

    private static final long MAX_PENDING_BYTES = 3;

    private S3Client s3Client;
    private SttAudioStorage storage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        storage = new SttAudioStorage(s3Client, PROPERTIES, CLOCK, new SyncTaskExecutor(), MAX_PENDING_BYTES);
    }

    @Nested
    class Save {

        @Test
        void 회원_토론_세션별_경로로_오디오를_저장한다() {
            storage.save(MEMBER_ID, SESSION_ID, AUDIO);

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
            MockMultipartFile blob = new MockMultipartFile("audio", "blob", "audio/webm", new byte[]{1});

            storage.save(MEMBER_ID, SESSION_ID, blob);

            ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(request.capture(), any(RequestBody.class));
            assertThat(request.getValue().key()).matches("stt-audio/7/session-1/20261103-130405-[0-9a-f-]{36}");
        }

        @Test
        void S3_저장에_실패해도_예외를_던지지_않는다() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(S3Exception.builder().message("denied").build());

            assertThatCode(() -> storage.save(MEMBER_ID, SESSION_ID, AUDIO)).doesNotThrowAnyException();
        }

        @Test
        void S3_클라이언트가_없으면_꺼진_것으로_보고_저장하지_않는다() {
            SttAudioStorage disabled = new SttAudioStorage(null, PROPERTIES, CLOCK, new SyncTaskExecutor(),
                    MAX_PENDING_BYTES);

            assertAll(
                    () -> assertThat(disabled.isEnabled()).isFalse(),
                    () -> assertThatCode(() -> disabled.save(MEMBER_ID, SESSION_ID, AUDIO)).doesNotThrowAnyException()
            );
        }

        @Test
        void 업로드를_기다리는_오디오가_상한을_넘으면_보관하지_않는다() {
            MockMultipartFile large = new MockMultipartFile("audio", "segment.webm", "audio/webm",
                    new byte[]{1, 2, 3, 4});

            storage.save(MEMBER_ID, SESSION_ID, large);

            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 업로드가_끝나면_대기_용량을_돌려받아_다음_오디오를_보관한다() {
            storage.save(MEMBER_ID, SESSION_ID, AUDIO);
            storage.save(MEMBER_ID, SESSION_ID, AUDIO);

            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 업로드_대기열이_가득_차도_예외를_던지지_않고_대기_용량을_돌려받는다() {
            AtomicBoolean full = new AtomicBoolean(true);
            TaskExecutor fullOnce = task -> {
                if (full.getAndSet(false)) {
                    throw new TaskRejectedException("full");
                }
                task.run();
            };
            SttAudioStorage storage = new SttAudioStorage(s3Client, PROPERTIES, CLOCK, fullOnce, MAX_PENDING_BYTES);

            assertThatCode(() -> storage.save(MEMBER_ID, SESSION_ID, AUDIO)).doesNotThrowAnyException();
            storage.save(MEMBER_ID, SESSION_ID, AUDIO);

            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 토론_세션_식별자가_없으면_보관하지_않는다() {
            storage.save(MEMBER_ID, null, AUDIO);

            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "../other", "a/b", "session id", "가나다"})
        void 토론_세션_식별자에_경로에_쓸_수_없는_문자가_있으면_보관하지_않는다(String sessionId) {
            assertThatCode(() -> storage.save(MEMBER_ID, sessionId, AUDIO)).doesNotThrowAnyException();

            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 토론_세션_식별자가_64자를_넘으면_보관하지_않는다() {
            storage.save(MEMBER_ID, "a".repeat(65), AUDIO);

            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 오디오를_읽다가_예상하지_못한_예외가_나도_던지지_않고_대기_용량을_돌려받는다() throws IOException {
            MultipartFile broken = mock(MultipartFile.class);
            when(broken.getSize()).thenReturn(3L);
            when(broken.getBytes()).thenThrow(new IllegalStateException("broken"));

            assertThatCode(() -> storage.save(MEMBER_ID, SESSION_ID, broken)).doesNotThrowAnyException();
            storage.save(MEMBER_ID, SESSION_ID, AUDIO);

            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 업로드_중_SDK_외의_예외가_나도_던지지_않고_대기_용량을_돌려받는다() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new IllegalStateException("unexpected"))
                    .thenReturn(null);

            assertThatCode(() -> storage.save(MEMBER_ID, SESSION_ID, AUDIO)).doesNotThrowAnyException();
            storage.save(MEMBER_ID, SESSION_ID, AUDIO);

            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }
}
