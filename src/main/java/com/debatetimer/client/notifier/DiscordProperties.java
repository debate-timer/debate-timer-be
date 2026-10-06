package com.debatetimer.client.notifier;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {

    private final String token;
    private final String channelId;
    private final String sharingChannelId;

    public DiscordProperties(String token, String channelId, String sharingChannelId) {
        validate(token);
        validate(channelId);
        validate(sharingChannelId);
        this.token = token;
        this.channelId = channelId;
        this.sharingChannelId = sharingChannelId;
    }

    private void validate(String element) {
        if (element == null || element.isBlank()) {
            throw new DTInitializationException(InitializationErrorCode.DISCORD_PROPERTIES_EMPTY);
        }
    }
}
