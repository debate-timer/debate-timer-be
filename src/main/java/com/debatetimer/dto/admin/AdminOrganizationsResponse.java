package com.debatetimer.dto.admin;

import com.debatetimer.domain.organization.Organization;
import java.util.List;

public record AdminOrganizationsResponse(List<AdminOrganizationResponse> organizations) {

    public static AdminOrganizationsResponse from(List<Organization> organizations) {
        return new AdminOrganizationsResponse(organizations.stream()
                .map(AdminOrganizationResponse::new)
                .toList());
    }
}
