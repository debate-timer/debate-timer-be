package com.debatetimer.client;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OauthProperties {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String grantType;

    public OauthProperties(
            @Value("${oauth.client_id}") String clientId,
            @Value("${oauth.client_secret}") String clientSecret,
            @Value("${oauth.redirect_uri}") String redirectUri,
            @Value("${oauth.grant_type}") String grantType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.grantType = grantType;
    }
}
