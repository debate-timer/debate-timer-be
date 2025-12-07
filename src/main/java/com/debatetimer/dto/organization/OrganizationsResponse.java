package com.debatetimer.dto.organization;

import com.debatetimer.domain.organization.Organization;
import java.util.List;

public record OrganizationsResponse(List<OrganizationResponse> organizations) {

    public static OrganizationsResponse from(List<Organization> organizations) {
        return new OrganizationsResponse(toOrganizationsResponse(organizations));
    }

    private static List<OrganizationResponse> toOrganizationsResponse(List<Organization> organizations) {
        return organizations.stream()
                .map(OrganizationResponse::new)
                .toList();
    }
}
