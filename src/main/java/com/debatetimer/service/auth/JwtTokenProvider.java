package com.debatetimer.service.auth;

import com.debatetimer.dto.member.MemberInfo;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public String createAccessTokren(MemberInfo memberInfo) {
        return null;
    }

    public String createRefreshToken(MemberInfo memberInfo) {
        return null;
    }
}
