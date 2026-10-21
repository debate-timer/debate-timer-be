package com.debatetimer.dto.admin;

import com.debatetimer.domain.organization.OrganizationTemplate;

public record AdminOrganizationTemplateResponse(long id, String name, String data) {

    public AdminOrganizationTemplateResponse(OrganizationTemplate template) {
        this(template.getId(), template.getName(), template.getData());
    }
}
