package com.debatetimer.client;

import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OauthTokenResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(OauthProperties.class)
public class OauthClient {

    private final RestClient restClient;
    private final OauthProperties oauthProperties;

    public OauthClient(OauthProperties oauthProperties) {
        this.restClient = RestClient.create();
        this.oauthProperties = oauthProperties;

    }

    public OauthTokenResponse requestToken(MemberCreateRequest request) {
        return restClient.put()
                .uri(uriBuilder -> uriBuilder.path("https://oauth2.googleapis.com/token")
                        .queryParam("code", request.code())
                        .queryParam("client_id", oauthProperties.getClientId())
                        .queryParam("client_secret", oauthProperties.getClientSecret())
                        .queryParam("redirect_uri", oauthProperties.getRedirectUri())
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(OauthTokenResponse.class);
    }

    public MemberInfo requestMemberInfo(OauthTokenResponse response) {
        return restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + response.accessToken())
                .retrieve()
                .body(MemberInfo.class);
    }
}
