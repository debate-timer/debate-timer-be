package com.debatetimer.client.storage;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import java.nio.file.Path;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "icon")
public class IconStorageProperties {

    private final Path uploadDir;

    public IconStorageProperties(String uploadDir) {
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new DTInitializationException(InitializationErrorCode.ICON_UPLOAD_DIR_EMPTY);
        }
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String getResourceLocation() {
        return uploadDir.toUri().toString().replaceAll("/?$", "/");
    }
}
