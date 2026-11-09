package com.debatetimer.dto.admin.response;

import com.debatetimer.domain.member.FeatureFlagGrant;
import com.debatetimer.domain.member.Member;
import java.util.List;

public record AdminMemberFeatureFlagResponse(
        long memberId,
        String email,
        List<AdminGrantedFeatureFlagResponse> featureFlags
) {

    public AdminMemberFeatureFlagResponse(Member member, List<FeatureFlagGrant> grants) {
        this(member.getId(), member.getEmail(), grants.stream()
                .map(AdminGrantedFeatureFlagResponse::new)
                .toList());
    }
}
