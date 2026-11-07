package com.debatetimer.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.client.storage.SttAudioStorageProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StorageConfigTest {

    private final StorageConfig storageConfig = new StorageConfig();

    @Nested
    class SttAudioStorage {

        @Test
        void 켜져_있고_설정이_모두_있으면_저장한다() {
            assertThat(storageConfig.sttAudioStorage(new SttAudioStorageProperties(
                    true, "bucket", "ap-northeast-2", "access", "secret", null)).isEnabled()).isTrue();
        }

        @Test
        void 꺼져_있으면_저장하지_않는다() {
            assertThat(storageConfig.sttAudioStorage(new SttAudioStorageProperties(
                    false, "bucket", "ap-northeast-2", "access", "secret", null)).isEnabled()).isFalse();
        }

        @Test
        void 켜져_있어도_설정이_비어_있으면_저장하지_않는다() {
            assertThat(storageConfig.sttAudioStorage(new SttAudioStorageProperties(
                    true, "", "ap-northeast-2", "access", "secret", null)).isEnabled()).isFalse();
        }
    }
}
