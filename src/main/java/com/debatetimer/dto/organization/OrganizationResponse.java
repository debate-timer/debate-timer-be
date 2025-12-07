package com.debatetimer.dto.organization;

import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationTemplate;
import java.util.List;

public record OrganizationResponse(
        String organization,
        String affiliation,
        String iconPath,
        List<OrganizationTemplatesResponse> templates
) {

    public OrganizationResponse(Organization organization) {
        this(
                organization.getName(),
                organization.getAffiliation(),
                organization.getIconPath(),
                toTemplatesResponse(organization.getTemplates())
        );
    }

    private static List<OrganizationTemplatesResponse> toTemplatesResponse(List<OrganizationTemplate> templates) {
        return templates.stream()
                .map(OrganizationTemplatesResponse::new)
                .toList();
    }
}
