package com.debatetimer.controller.admin;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 어드민 비밀번호를 확인한다. 비교 시간으로 비밀번호를 추측할 수 없도록 고정 시간 비교를 쓴다.
 */
@Component
@RequiredArgsConstructor
public class AdminAuthorizer {

    private final AdminProperties adminProperties;

    public void authorize(@Nullable String password) {
        if (password == null || !MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                adminProperties.getPassword().getBytes(StandardCharsets.UTF_8))) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_ADMIN_PASSWORD);
        }
    }
}
