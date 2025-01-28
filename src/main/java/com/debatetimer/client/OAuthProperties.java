package com.debatetimer.client;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String grantType;

    public OAuthProperties(
            String clientId,
            String clientSecret,
            String redirectUri,
            String grantType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.grantType = grantType;
    }
}
