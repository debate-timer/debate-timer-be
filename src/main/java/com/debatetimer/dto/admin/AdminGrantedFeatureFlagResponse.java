package com.debatetimer.dto.admin;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.FeatureFlagGrant;
import java.time.LocalDateTime;

public record AdminGrantedFeatureFlagResponse(
        FeatureFlag featureFlag,
        LocalDateTime grantedAt
) {

    public AdminGrantedFeatureFlagResponse(FeatureFlagGrant grant) {
        this(grant.featureFlag(), grant.grantedAt());
    }
}
