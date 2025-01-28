package com.debatetimer.client;

import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OAuthTokenResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthClient {

    private final RestClient restClient;
    private final OAuthProperties oauthProperties;

    public OAuthClient(OAuthProperties oauthProperties) {
        this.restClient = RestClient.create();
        this.oauthProperties = oauthProperties;
    }

    public OAuthTokenResponse requestToken(MemberCreateRequest request) {
        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(oauthProperties.createTokenRequestBody(request))
                .retrieve()
                .body(OAuthTokenResponse.class);
    }

    public MemberInfo requestMemberInfo(OAuthTokenResponse response) {
        return restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + response.access_token())
                .retrieve()
                .body(MemberInfo.class);
    }
}
