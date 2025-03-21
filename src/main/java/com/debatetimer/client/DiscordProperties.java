package com.debatetimer.client;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {

    private final String token;
    private final String channelId;

    public DiscordProperties(String token, String channelId) {
        validate(token);
        validate(channelId);
        this.token = token;
        this.channelId = channelId;
    }

    private void validate(String element) {
        if (element == null || element.isBlank()) {
            throw new DTInitializationException(InitializationErrorCode.OAUTH_PROPERTIES_EMPTY);
        }
    }
}
