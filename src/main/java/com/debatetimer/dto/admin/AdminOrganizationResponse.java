package com.debatetimer.dto.admin;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import java.util.List;

public record AdminOrganizationResponse(
        long id,
        String name,
        String affiliation,
        String iconPath,
        Language language,
        List<AdminOrganizationTemplateResponse> templates
) {

    public AdminOrganizationResponse(Organization organization) {
        this(
                organization.getId(),
                organization.getName(),
                organization.getAffiliation(),
                organization.getIconPath(),
                organization.getLanguage(),
                organization.getTemplates()
                        .stream()
                        .map(AdminOrganizationTemplateResponse::new)
                        .toList()
        );
    }
}
