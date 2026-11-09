package com.debatetimer.dto.admin.request;

import com.debatetimer.domain.member.FeatureFlag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureFlagGrantRequest(
        @NotBlank @Email
        String email,

        @NotNull
        FeatureFlag featureFlag
) {
}
