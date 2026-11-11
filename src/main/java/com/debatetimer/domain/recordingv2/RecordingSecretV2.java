package com.debatetimer.domain.recordingv2;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 녹음 참가 코드·기기 토큰을 만들고 해시한다. 원문은 발급 응답에만 담고, 서버에는 해시만 남긴다.
 */
public final class RecordingSecretV2 {

    private static final SecureRandom RANDOM = new SecureRandom();
    // 참가 코드는 공유 링크·QR에 들어가므로 짧게, 기기 토큰은 길게 만든다.
    private static final int JOIN_CODE_BYTES = 12;
    private static final int DEVICE_TOKEN_BYTES = 32;
    private static final int DEVICE_ID_BYTES = 6;

    private RecordingSecretV2() {
    }

    public static String generateJoinCode() {
        return generate(JOIN_CODE_BYTES);
    }

    public static String generateDeviceToken() {
        return generate(DEVICE_TOKEN_BYTES);
    }

    // S3 경로에 들어가므로 URL·경로에 안전한 문자만 쓴다.
    public static String generateDeviceId() {
        return generate(DEVICE_ID_BYTES);
    }

    public static String hash(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 쓸 수 없습니다.", exception);
        }
    }

    public static boolean matches(String secret, String hash) {
        return MessageDigest.isEqual(hash(secret).getBytes(StandardCharsets.US_ASCII),
                hash.getBytes(StandardCharsets.US_ASCII));
    }

    private static String generate(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}
