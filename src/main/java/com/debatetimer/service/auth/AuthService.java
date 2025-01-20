package com.debatetimer.service.auth;

import com.debatetimer.client.OauthClient;
import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OauthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OauthClient oauthClient;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberInfo getMemberInfo(MemberCreateRequest request) {
        OauthTokenResponse oauthTokenResponse = oauthClient.requestToken(request);
        return oauthClient.requestMemberInfo(oauthTokenResponse);
    }

    public JwtTokenResponse createToken(MemberInfo memberInfo) {
        String accessToken = jwtTokenProvider.createAccessTokren(memberInfo);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberInfo);
        return new JwtTokenResponse(accessToken, refreshToken);
    }
}
