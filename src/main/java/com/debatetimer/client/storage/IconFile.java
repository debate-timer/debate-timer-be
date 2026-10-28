package com.debatetimer.client.storage;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;

/**
 * 아이콘 파일. 내용은 필요할 때 스트림으로 읽어 여러 파일을 한꺼번에 메모리에 올리지 않는다.
 */
public record IconFile(String fileName, Resource resource) {

    public InputStream open() throws IOException {
        return resource.getInputStream();
    }
}
