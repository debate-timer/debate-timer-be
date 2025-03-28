package com.debatetimer.service.auth;

import com.debatetimer.client.oauth.OAuthClient;
import com.debatetimer.domain.member.Member;
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

    public MemberInfo getMemberInfo(MemberCreateRequest request) {
        OAuthToken oauthToken = oauthClient.requestToken(request);
        return oauthClient.requestMemberInfo(oauthToken);
    }

    public Member getMember(String email) {
        return memberRepository.getByEmail(email);
    }

    public void logout(Member member, String email) {
        if (!member.isSameMember(email)) {
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }
    }
}
