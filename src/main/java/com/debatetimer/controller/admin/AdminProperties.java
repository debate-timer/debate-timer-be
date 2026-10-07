package com.debatetimer.controller.admin;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private final String password;

    public AdminProperties(String password) {
        if (password == null || password.isBlank()) {
            throw new DTInitializationException(InitializationErrorCode.ADMIN_PASSWORD_EMPTY);
        }
        this.password = password;
    }
}
