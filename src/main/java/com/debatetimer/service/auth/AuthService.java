package com.debatetimer.service.auth;

import com.debatetimer.client.OAuthClient;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OAuthToken;
import com.debatetimer.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthClient oauthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenResolver jwtTokenResolver;
    private final MemberRepository memberRepository;

    public MemberInfo getMemberInfo(MemberCreateRequest request) {
        OAuthToken oauthToken = oauthClient.requestToken(request);
        return oauthClient.requestMemberInfo(oauthToken);
    }

    public JwtTokenResponse createToken(MemberInfo memberInfo) {
        String accessToken = jwtTokenProvider.createAccessToken(memberInfo);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberInfo);
        return new JwtTokenResponse(accessToken, refreshToken);
    }

    public Member getMember(String accessToken) {
        String nickname = jwtTokenResolver.resolveAccessToken(accessToken);
        return memberRepository.getByNickname(nickname);
    }

    public JwtTokenResponse reissueToken(String refreshToken) {
        String nickname = jwtTokenResolver.resolveRefreshToken(refreshToken);
        MemberInfo memberInfo = new MemberInfo(nickname);
        String accessToken = jwtTokenProvider.createAccessToken(memberInfo);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberInfo);
        return new JwtTokenResponse(accessToken, newRefreshToken);
    }
}
