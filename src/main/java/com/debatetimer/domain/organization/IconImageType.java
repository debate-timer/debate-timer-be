package com.debatetimer.domain.organization;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * 업로드할 수 있는 아이콘 이미지 형식. 확장자 대신 파일 앞부분의 시그니처로 형식을 판별한다.
 * 스크립트를 담을 수 있는 SVG는 허용하지 않는다.
 */
public enum IconImageType {

    PNG("png", content -> startsWith(content, 0, new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'})),
    JPEG("jpg", content -> startsWith(content, 0, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})),
    WEBP("webp", content -> startsWith(content, 0, "RIFF".getBytes()) && startsWith(content, 8, "WEBP".getBytes())),
    ;

    private final String extension;
    private final Predicate<byte[]> signatureMatcher;

    IconImageType(String extension, Predicate<byte[]> signatureMatcher) {
        this.extension = extension;
        this.signatureMatcher = signatureMatcher;
    }

    public static IconImageType from(byte[] content) {
        return Arrays.stream(values())
                .filter(type -> type.signatureMatcher.test(content))
                .findFirst()
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.INVALID_ICON_FILE));
    }

    private static boolean startsWith(byte[] content, int offset, byte[] signature) {
        if (content.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[offset + i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    public String getExtension() {
        return extension;
    }
}
