package com.debatetimer.client.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class SttAudioStoragePropertiesTest {

    @Test
    void 설정하지_않은_값은_기본값을_쓴다() {
        SttAudioStorageProperties properties = new SttAudioStorageProperties(null, null, null, null, null, null);

        assertAll(
                () -> assertThat(properties.enabled()).isFalse(),
                () -> assertThat(properties.keyPrefix()).isEqualTo("stt-audio")
        );
    }

    @Test
    void 버킷_리전_키가_모두_있어야_설정이_완료된_것이다() {
        assertAll(
                () -> assertThat(new SttAudioStorageProperties(true, "bucket", "ap-northeast-2", "access", "secret",
                        null).isComplete()).isTrue(),
                () -> assertThat(new SttAudioStorageProperties(true, "bucket", "ap-northeast-2", "access", " ",
                        null).isComplete()).isFalse()
        );
    }
}
