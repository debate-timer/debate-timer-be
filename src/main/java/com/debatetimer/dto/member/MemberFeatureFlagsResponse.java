package com.debatetimer.dto.member;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.MemberFeatureFlags;
import java.util.List;

public record MemberFeatureFlagsResponse(List<FeatureFlag> featureFlags) {

    public MemberFeatureFlagsResponse(MemberFeatureFlags flags) {
        this(flags.values());
    }
}
