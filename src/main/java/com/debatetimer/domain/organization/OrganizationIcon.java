package com.debatetimer.domain.organization;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * 단체 아이콘 이미지. 파일 이름은 경로 조작을 막기 위해 영문 소문자, 숫자, '_', '-'만 쓰고 확장자는 이미지 형식으로 정한다.
 */
@Getter
public class OrganizationIcon {

    public static final String PATH_PREFIX = "/icon/";
    public static final int MAX_SIZE_BYTES = 2 * 1024 * 1024;
    public static final int MAX_NAME_LENGTH = 50;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_-]{1,%d}$".formatted(MAX_NAME_LENGTH));
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.[A-Za-z0-9]+$");

    private final String fileName;
    private final byte[] content;

    public OrganizationIcon(String name, byte[] content) {
        validateSize(content);
        IconImageType type = IconImageType.from(content);
        this.fileName = normalizeName(name) + "." + type.getExtension();
        this.content = content;
    }

    private void validateSize(byte[] content) {
        if (content == null || content.length == 0 || content.length > MAX_SIZE_BYTES) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_ICON_FILE);
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_ICON_NAME);
        }
        String normalized = EXTENSION_PATTERN.matcher(name.strip()).replaceFirst("")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
        if (!NAME_PATTERN.matcher(normalized).matches()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_ICON_NAME);
        }
        return normalized;
    }

    public String getPath() {
        return PATH_PREFIX + fileName;
    }
}
