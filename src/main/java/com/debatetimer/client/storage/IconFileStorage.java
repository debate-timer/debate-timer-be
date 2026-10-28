package com.debatetimer.client.storage;

import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * 아이콘 파일을 다룬다. 배포에 포함된 기본 아이콘(classpath:/static/icon)은 읽기만 하고,
 * 업로드한 아이콘은 jar 밖의 업로드 디렉터리에 저장한다. 두 위치 모두 /icon/** 로 제공된다.
 */
@Component
public class IconFileStorage {

    private static final String BUNDLED_ICON_LOCATION = "static/icon/";
    private static final String BUNDLED_ICON_PATTERN = "classpath:/" + BUNDLED_ICON_LOCATION + "*";

    private final Path uploadDir;
    private final List<String> bundledFileNames;

    public IconFileStorage(IconStorageProperties properties) {
        this.uploadDir = properties.getUploadDir();
        this.bundledFileNames = loadBundledFileNames();
        createUploadDir();
    }

    private List<String> loadBundledFileNames() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(BUNDLED_ICON_PATTERN);
            return Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            return List.of();
        }
    }

    private void createUploadDir() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
    }

    public List<String> findBundledFileNames() {
        return bundledFileNames;
    }

    public List<String> findUploadedFileNames() {
        try (Stream<Path> files = Files.list(uploadDir)) {
            return files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
    }

    public boolean exists(String fileName) {
        return bundledFileNames.contains(fileName) || Files.exists(uploadDir.resolve(fileName));
    }

    /**
     * 같은 이름의 파일이 이미 있으면 덮어쓰지 않고 false를 반환한다.
     */
    public boolean saveIfAbsent(String fileName, byte[] content) {
        try {
            Files.write(uploadDir.resolve(fileName), content, StandardOpenOption.CREATE_NEW);
            return true;
        } catch (FileAlreadyExistsException exception) {
            return false;
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
    }

    /**
     * 기본 아이콘과 업로드한 아이콘을 원본 그대로 모두 읽는다.
     */
    public List<IconFile> findAll() {
        return Stream.concat(
                bundledFileNames.stream().map(this::readBundled),
                findUploadedFileNames().stream().map(this::readUploaded)
        ).toList();
    }

    private IconFile readBundled(String fileName) {
        try {
            byte[] content = new ClassPathResource(BUNDLED_ICON_LOCATION + fileName).getContentAsByteArray();
            return new IconFile(fileName, content);
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
    }

    private IconFile readUploaded(String fileName) {
        try {
            return new IconFile(fileName, Files.readAllBytes(uploadDir.resolve(fileName)));
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
    }
}
