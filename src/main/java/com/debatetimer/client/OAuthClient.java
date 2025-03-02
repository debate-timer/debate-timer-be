package com.debatetimer.client;

import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OAuthToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthClient {

    private final RestClient restClient;
    private final OAuthProperties oauthProperties;

    public OAuthClient(OAuthProperties oauthProperties) {
        this.restClient = RestClient.create();
        this.oauthProperties = oauthProperties;
    }

    public OAuthToken requestToken(MemberCreateRequest request) {
        OAuthToken oAuthToken = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(oauthProperties.createTokenRequestBody(request))
                .retrieve()
                .body(OAuthToken.class);
        log.info("구글 액세스 토근 발급 성공");
        return oAuthToken;
    }

    public MemberInfo requestMemberInfo(OAuthToken response) {
        MemberInfo memberInfo = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .headers(headers -> headers.setBearerAuth(response.access_token()))
                .retrieve()
                .body(MemberInfo.class);
        log.info("구글 회원정보 조회 성공");
        return memberInfo;
    }
}
