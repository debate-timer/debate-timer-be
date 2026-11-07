package com.debatetimer.client.storage;

import org.springframework.lang.Nullable;

/**
 * S3에 보관할 발언 구간 오디오 한 개. 요청이 끝나면 업로드 임시 파일이 지워지므로 내용을 바이트로 들고 비동기로 넘긴다.
 */
public record SttAudio(
        long memberId,
        String sessionId,
        byte[] content,
        @Nullable String contentType,
        @Nullable String originalFilename
) {
}
