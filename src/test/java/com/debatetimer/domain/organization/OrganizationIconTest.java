package com.debatetimer.domain.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrganizationIconTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0};
    static final byte[] WEBP = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);

    @Nested
    class Create {

        @Test
        void 파일_내용으로_형식을_판별해_확장자를_정한다() {
            assertAll(
                    () -> assertThat(new OrganizationIcon("png_icon", PNG).getFileName()).isEqualTo("png_icon.png"),
                    () -> assertThat(new OrganizationIcon("jpeg_icon", JPEG).getFileName()).isEqualTo("jpeg_icon.jpg"),
                    () -> assertThat(new OrganizationIcon("webp_icon", WEBP).getFileName()).isEqualTo("webp_icon.webp")
            );
        }

        @Test
        void 원래_확장자를_떼고_소문자와_밑줄로_정규화한다() {
            OrganizationIcon icon = new OrganizationIcon(" Han Alm.JPG ", PNG);

            assertAll(
                    () -> assertThat(icon.getFileName()).isEqualTo("han_alm.png"),
                    () -> assertThat(icon.getPath()).isEqualTo("/icon/han_alm.png")
            );
        }

        @Test
        void 이미지가_아니면_예외를_던진다() {
            byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(() -> new OrganizationIcon("icon", svg))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_ICON_FILE.getMessage());
        }

        @Test
        void 최대_크기를_넘으면_예외를_던진다() {
            byte[] content = new byte[OrganizationIcon.MAX_SIZE_BYTES + 1];
            System.arraycopy(PNG, 0, content, 0, PNG.length);

            assertThatThrownBy(() -> new OrganizationIcon("icon", content))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_ICON_FILE.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "../evil", "한앎", "a/b", "icon.name.png"})
        void 허용하지_않는_이름이면_예외를_던진다(String name) {
            assertThatThrownBy(() -> new OrganizationIcon(name, PNG))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_ICON_NAME.getMessage());
        }
    }
}
