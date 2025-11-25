package com.debatetimer.config;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@ConfigurationProperties(prefix = "cors.origin")
public class CorsProperties {

    private final String[] corsOrigin;

    //TODO 머지될 때 dev, prod secret 갱신 필요
    public CorsProperties(String[] corsOrigin) {
        validate(corsOrigin);
        this.corsOrigin = corsOrigin;
    }

    private void validate(String[] corsOrigin) {
        if (corsOrigin == null || corsOrigin.length == 0) {
            throw new DTInitializationException(InitializationErrorCode.CORS_ORIGIN_EMPTY);
        }
        for (String origin : corsOrigin) {
            if (origin == null || origin.isBlank()) {
                throw new DTInitializationException(InitializationErrorCode.CORS_ORIGIN_STRING_BLANK);
            }
        }
    }
}
