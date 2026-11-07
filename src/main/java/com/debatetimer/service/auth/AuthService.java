package com.debatetimer.service.auth;

import com.debatetimer.client.oauth.OAuthClient;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.member.MemberFeatureFlags;
import com.debatetimer.domainrepository.member.MemberFeatureFlagDomainRepository;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OAuthToken;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthClient oauthClient;
    private final MemberRepository memberRepository;
    private final MemberFeatureFlagDomainRepository memberFeatureFlagDomainRepository;

    public MemberInfo getMemberInfo(MemberCreateRequest request) {
        OAuthToken oauthToken = oauthClient.requestToken(request);
        return oauthClient.requestMemberInfo(oauthToken);
    }

    public Member getMember(String email) {
        return memberRepository.getByEmail(email);
    }

    public void authorizeFeatureFlag(Member member, FeatureFlag... requiredFlags) {
        if (requiredFlags.length == 0) {
            return;
        }
        MemberFeatureFlags flags = memberFeatureFlagDomainRepository.getFlags(member.getId());
        if (!flags.hasAll(requiredFlags)) {
            throw new DTClientErrorException(ClientErrorCode.FEATURE_NOT_ALLOWED);
        }
    }

    public void logout(Member member, String email) {
        if (!member.isSameMember(email)) {
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }
    }
}
