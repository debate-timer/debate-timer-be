package com.debatetimer.dto.admin.request;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationCategory;
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
        Language language,

        @NotNull
        OrganizationCategory category
) {

    public Organization toOrganization() {
        return new Organization(name, affiliation, iconPath, language, category);
    }
}
