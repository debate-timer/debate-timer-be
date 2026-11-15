package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RecordingTasksV2Test {

    @Test
    void 작업이_끝까지_실행되면_true를_돌려준다() {
        assertThat(RecordingTasksV2.runQuietly("작업", "대상", () -> {
        })).isTrue();
    }

    @Test
    void 작업이_실패하면_예외를_던지지_않고_false를_돌려준다() {
        assertThat(RecordingTasksV2.runQuietly("작업", "대상", () -> {
            throw new IllegalStateException("fail");
        })).isFalse();
    }

    @Test
    void 결과가_있는_작업은_실패하면_빈_값을_돌려준다() {
        assertThat(RecordingTasksV2.callQuietly("작업", "대상", () -> "ok")).contains("ok");
        assertThat(RecordingTasksV2.callQuietly("작업", "대상", () -> {
            throw new IllegalStateException("fail");
        })).isEmpty();
    }
}
