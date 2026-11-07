package com.debatetimer.dto.admin;

import com.debatetimer.domain.member.FeatureFlag;

public record AdminFeatureFlagResponse(
        FeatureFlag featureFlag,
        String description
) {

    public AdminFeatureFlagResponse(FeatureFlag featureFlag) {
        this(featureFlag, featureFlag.getDescription());
    }
}
