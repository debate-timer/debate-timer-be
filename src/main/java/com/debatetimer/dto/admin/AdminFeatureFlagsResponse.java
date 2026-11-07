package com.debatetimer.dto.admin;

import com.debatetimer.domain.member.FeatureFlag;
import java.util.Arrays;
import java.util.List;

public record AdminFeatureFlagsResponse(List<AdminFeatureFlagResponse> featureFlags) {

    public static AdminFeatureFlagsResponse all() {
        return new AdminFeatureFlagsResponse(Arrays.stream(FeatureFlag.values())
                .map(AdminFeatureFlagResponse::new)
                .toList());
    }
}
