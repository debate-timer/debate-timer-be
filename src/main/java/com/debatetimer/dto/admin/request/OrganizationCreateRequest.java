package com.debatetimer.dto.admin.request;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationCreateRequest(
        @NotBlank @Size(max = 255)
        String name,

        @NotNull @Size(max = 255)
        String affiliation,

        @NotBlank @Size(max = 255)
        String iconPath,

        @NotNull
        Language language
) {

    public Organization toOrganization() {
        return new Organization(name, affiliation, iconPath, language);
    }
}
