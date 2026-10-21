package com.debatetimer.client.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class IconStoragePropertiesTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 업로드_디렉터리가_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new IconStorageProperties(empty))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.ICON_UPLOAD_DIR_EMPTY.getMessage());
        }
    }

    @Nested
    class GetResourceLocation {

        @Test
        void 슬래시로_끝나는_파일_경로를_반환한다() {
            IconStorageProperties properties = new IconStorageProperties("/tmp/icon");

            assertThat(properties.getResourceLocation()).startsWith("file:").endsWith("/tmp/icon/");
        }
    }
}
