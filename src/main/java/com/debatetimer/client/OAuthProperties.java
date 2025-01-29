package com.debatetimer.client;

import com.debatetimer.dto.member.MemberCreateRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    public MultiValueMap<String, String> createTokenRequestBody(MemberCreateRequest request) {
        String code = request.code();
        String decode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        map.add("client_id", clientId);
        map.add("redirect_uri", redirectUri);
        map.add("code", decode);
        map.add("client_secret", clientSecret);

        return map;
    }
}
