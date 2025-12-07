package com.debatetimer.dto.organization;

import com.debatetimer.domain.organization.OrganizationTemplate;

public record OrganizationTemplatesResponse(String name, String data) {

    public OrganizationTemplatesResponse(OrganizationTemplate template) {
        this(template.getName(), template.getData());
    }
}
