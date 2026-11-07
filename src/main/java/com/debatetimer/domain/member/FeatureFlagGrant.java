package com.debatetimer.domain.member;

import java.time.LocalDateTime;

public record FeatureFlagGrant(
        long memberId,
        FeatureFlag featureFlag,
        LocalDateTime grantedAt
) {
}
