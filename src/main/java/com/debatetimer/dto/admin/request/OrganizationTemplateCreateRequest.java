package com.debatetimer.dto.admin.request;

import com.debatetimer.domain.organization.OrganizationTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationTemplateCreateRequest(
        @NotBlank @Size(max = 255)
        String name,

        @NotBlank @Size(max = 8191)
        String data
) {

    public OrganizationTemplate toTemplate() {
        return new OrganizationTemplate(name, data);
    }
}
