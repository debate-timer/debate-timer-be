package com.debatetimer.client.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class AudioSpoolV2Test {

    @TempDir
    private Path directory;

    @Test
    void 오디오를_확장자를_살려_임시_파일로_받는다() throws IOException {
        AudioSpoolV2 spool = new AudioSpoolV2(directory, 10);

        Path file = spool.save(audio("turn.webm", 3));

        assertThat(file.getParent()).isEqualTo(directory);
        assertThat(file.getFileName().toString()).endsWith(".webm");
        assertThat(Files.readAllBytes(file)).hasSize(3);
        assertThat(spool.getUsedBytes()).isEqualTo(3);
    }

    @Test
    void 경로_문자가_섞인_확장자는_버린다() {
        AudioSpoolV2 spool = new AudioSpoolV2(directory, 10);

        Path file = spool.save(audio("turn.we/../bm", 1));

        assertThat(file.getParent()).isEqualTo(directory);
        assertThat(file.getFileName().toString()).doesNotContain(".");
    }

    @Test
    void 용량을_넘으면_받지_않는다() {
        AudioSpoolV2 spool = new AudioSpoolV2(directory, 4);
        spool.save(audio("a.webm", 3));

        assertThatThrownBy(() -> spool.save(audio("b.webm", 2)))
                .isInstanceOf(DTClientErrorException.class)
                .hasMessage(ClientErrorCode.RECORDING_BUSY.getMessage());
        assertThat(spool.getUsedBytes()).isEqualTo(3);
    }

    @Test
    void 지우면_용량을_돌려받는다() {
        AudioSpoolV2 spool = new AudioSpoolV2(directory, 4);
        Path file = spool.save(audio("a.webm", 3));

        spool.delete(file);

        assertThat(file).doesNotExist();
        assertThat(spool.getUsedBytes()).isZero();
    }

    @Test
    void 남아_있던_파일도_사용량에_넣고_오래된_파일만_정리한다() throws IOException {
        Instant now = Instant.parse("2026-11-12T00:00:00Z");
        Path old = Files.write(directory.resolve("old.webm"), new byte[]{1, 2});
        Path recent = Files.write(directory.resolve("recent.webm"), new byte[]{1});
        Files.setLastModifiedTime(old, FileTime.from(now.minus(Duration.ofDays(2))));
        Files.setLastModifiedTime(recent, FileTime.from(now.minusSeconds(10)));

        AudioSpoolV2 spool = new AudioSpoolV2(directory, 10);
        assertThat(spool.getUsedBytes()).isEqualTo(3);

        spool.deleteOlderThan(Duration.ofDays(1), Clock.fixed(now, ZoneOffset.UTC));

        assertThat(old).doesNotExist();
        assertThat(recent).exists();
        assertThat(spool.getUsedBytes()).isEqualTo(1);
    }

    private MockMultipartFile audio(String filename, int size) {
        return new MockMultipartFile("audio", filename, "audio/webm", new byte[size]);
    }
}
