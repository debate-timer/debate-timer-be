package com.debatetimer.dto.organization;

import com.debatetimer.domain.organization.OrganizationTemplate;

public record OrganizationTemplateResponse(String name, String data) {

    public OrganizationTemplateResponse(OrganizationTemplate template) {
        this(template.getName(), template.getData());
    }
}
